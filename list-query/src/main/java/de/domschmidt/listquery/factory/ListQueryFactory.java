package de.domschmidt.listquery.factory;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import de.domschmidt.listquery.dto.request.EnumQuerySort;
import de.domschmidt.listquery.dto.request.ListFieldQuery;
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import de.domschmidt.listquery.dto.response.ListPage;
import de.domschmidt.listquery.dto.response.items.ListItem;
import jakarta.persistence.EntityManager;
import java.util.*;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListQueryFactory<Entity> {

    private final EntityManager entityManager;
    private final EntityPathBase<Entity> qClazz;
    private final Expression<?> itemIdExpression;
    private final ListQuery predicate;

    @Setter
    private OrderSpecifier<?> defaultOrder;

    private BooleanExpression defaultFilter;

    private final Map<String, Expression<?>> expressionQuery = new HashMap<>();
    private final Set<Path<?>> joins = new HashSet<>();

    private final List<String> defaultListSelection = new ArrayList<>();

    public ListQueryFactory(
            final EntityManager entityManager,
            final EntityPathBase<Entity> qClazz,
            final Expression<?> itemIdExpression,
            final ListQuery predicate) {
        this.entityManager = entityManager;
        this.qClazz = qClazz;
        this.itemIdExpression = itemIdExpression;
        this.predicate = predicate;
    }

    /**
     * Add an expression.
     *
     * @param alias the column alias (represented in the result)
     * @param expression the query expression
     * @param joins list of joins (if necessary)
     */
    public void addFetchExpr(final String alias, final Expression<?> expression, final Path<?>... joins) {
        if (this.defaultListSelection.contains(alias)) {
            throw new IllegalArgumentException("Duplicate alias found: " + alias);
        }
        this.defaultListSelection.add(alias);
        this.expressionQuery.put(alias, expression);
        if (joins != null) {
            this.joins.addAll(Arrays.asList(joins));
        }
    }

    public ListPage create() {
        final Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers = new HashMap<>();
        BooleanExpression filters = null;
        BooleanExpression globalFilters = null;

        final List<String> requestedSelection;
        if (this.predicate != null
                && this.predicate.getFieldSelection() != null
                && !this.predicate.getFieldSelection().isEmpty()) {
            requestedSelection = this.predicate.getFieldSelection();
        } else {
            requestedSelection = this.defaultListSelection;
        }

        final List<Expression<?>> querySelection = new ArrayList<>();

        final Map<String, ListFieldQuery> fieldPredicates = new HashMap<>();
        for (final Map.Entry<String, Expression<?>> pathQueryEntry : this.expressionQuery.entrySet()) {
            final Expression<?> currentPath = pathQueryEntry.getValue();
            querySelection.add(currentPath);

            final ListFieldQuery predicateValue;
            if (this.predicate != null
                    && this.predicate.getFieldPredicates() != null
                    && this.predicate.getFieldPredicates().get(pathQueryEntry.getKey()) != null) {
                predicateValue = this.predicate.getFieldPredicates().get(pathQueryEntry.getKey());
                fieldPredicates.put(pathQueryEntry.getKey(), predicateValue);
            } else {
                predicateValue = null;
            }

            final IListFilter iListFilter = FilterResolver.resolveFilter(currentPath.getType());
            if (iListFilter != null && predicateValue != null && predicateValue.getPredicates() != null) {

                BooleanExpression fieldFilters = null;

                final Map<String, List<QueryPredicate>> orGroups = new HashMap<>();
                final List<QueryPredicate> andGroup = new ArrayList<>();

                for (final QueryPredicate currentPredicate : predicateValue.getPredicates()) {
                    if (currentPredicate.getOrGroupIdentifier() != null) {
                        orGroups.computeIfAbsent(currentPredicate.getOrGroupIdentifier(), k -> new ArrayList<>())
                                .add(currentPredicate);
                    } else {
                        andGroup.add(currentPredicate);
                    }
                }

                for (final List<QueryPredicate> group : orGroups.values()) {
                    BooleanExpression orExpression = null;
                    for (final QueryPredicate currentPredicate : group) {
                        final BooleanExpression filter =
                                iListFilter.buildSearchExpression(pathQueryEntry.getValue(), currentPredicate);
                        if (filter != null) {
                            orExpression = orExpression == null ? filter : orExpression.or(filter);
                        }
                    }
                    if (orExpression != null) {
                        fieldFilters = fieldFilters == null ? orExpression : fieldFilters.and(orExpression);
                    }
                }

                for (final QueryPredicate currentPredicate : andGroup) {
                    final BooleanExpression filter =
                            iListFilter.buildSearchExpression(pathQueryEntry.getValue(), currentPredicate);
                    if (filter != null) {
                        fieldFilters = fieldFilters == null ? filter : fieldFilters.and(filter);
                    }
                }

                if (fieldFilters != null) {
                    if (filters != null) {
                        filters = filters.and(fieldFilters);
                    } else {
                        filters = fieldFilters;
                    }
                }
            }
            if (predicateValue != null) {
                Integer sortRanking = predicateValue.getSortRanking();
                if (sortRanking != null) {
                    queryOrderSpecifiers.put(
                            sortRanking,
                            new OrderSpecifier(
                                    predicateValue.getSort() == EnumQuerySort.DESC ? Order.DESC : Order.ASC,
                                    pathQueryEntry.getValue()));
                }
            }
        }

        if (predicate != null
                && predicate.getGlobalSearchTerm() != null
                && !predicate.getGlobalSearchTerm().isEmpty()) {
            for (final String currentSearchTerm :
                    predicate.getGlobalSearchTerm().split(" ")) {
                BooleanExpression currentSearchTermGlobalFilter = null;

                for (final String currentSelection : requestedSelection) {

                    final Expression<?> pathQuery = this.expressionQuery.get(currentSelection);
                    if (pathQuery != null) {
                        final IListFilter iListFilter = FilterResolver.resolveFilter(pathQuery.getType());
                        if (iListFilter != null) {
                            final BooleanExpression globalFilter =
                                    iListFilter.buildGlobalSearchExpression(pathQuery, currentSearchTerm);
                            if (globalFilter != null) {
                                if (currentSearchTermGlobalFilter != null) {
                                    currentSearchTermGlobalFilter = currentSearchTermGlobalFilter.or(globalFilter);
                                } else {
                                    currentSearchTermGlobalFilter = globalFilter;
                                }
                            }
                        }
                    }
                }

                if (currentSearchTermGlobalFilter != null) {
                    if (globalFilters != null) {
                        globalFilters = globalFilters.and(currentSearchTermGlobalFilter);
                    } else {
                        globalFilters = currentSearchTermGlobalFilter;
                    }
                }
            }
        }

        int queryPageSize = this.predicate != null
                        && this.predicate.getLimit() != null
                        && this.predicate.getLimit() > 0
                        && this.predicate.getLimit() <= 1000
                ? this.predicate.getLimit()
                : 200;
        int page = this.predicate != null && this.predicate.getPage() != null && this.predicate.getPage() > 0
                ? this.predicate.getPage()
                : 0;
        int queryOffset = page * queryPageSize;

        BooleanExpression queryFilterUnion = filters;
        if (queryFilterUnion == null) {
            queryFilterUnion = globalFilters;
        } else {
            queryFilterUnion = queryFilterUnion.and(globalFilters);
        }

        if (queryFilterUnion == null) {
            queryFilterUnion = this.defaultFilter;
        } else {
            queryFilterUnion = queryFilterUnion.and(this.defaultFilter);
        }

        OrderSpecifier<?>[] orderSpecifier = queryOrderSpecifiers.keySet().stream()
                .sorted()
                .map(queryOrderSpecifiers::get)
                .toArray(OrderSpecifier[]::new);
        if (orderSpecifier.length == 0 && this.defaultOrder != null) {
            orderSpecifier = new OrderSpecifier<?>[] {this.defaultOrder};
        }

        JPAQuery<?> queryBase = new JPAQuery<>(this.entityManager)
                .select(querySelection.toArray(new Expression<?>[] {}))
                .from(this.qClazz)
                .orderBy(orderSpecifier)
                .where(queryFilterUnion)
                .limit(queryPageSize + 1)
                .offset(queryOffset);

        for (final Object join : this.joins) {
            queryBase = queryBase.leftJoin((EntityPath<?>) join);
        }

        final List<Tuple> fetchBag = (List<Tuple>) queryBase.fetch();
        final List<ListItem> results = new ArrayList<>();
        for (int resultCount = 0; resultCount < Math.min(fetchBag.size(), queryPageSize); resultCount++) {
            final Map<String, Object> values = new HashMap<>();
            final Tuple currentResult = fetchBag.get(resultCount);

            for (final String currentSelection : requestedSelection) {
                final Expression<?> expressionQuery = this.expressionQuery.get(currentSelection);
                if (expressionQuery != null) {
                    values.put(currentSelection, currentResult.get(expressionQuery));
                }
            }

            Object itemId = currentResult.get(this.itemIdExpression);
            results.add(new ListItem(itemId != null ? itemId.toString() : null, values));
        }

        return new ListPage(
                this.predicate != null && this.predicate.getFieldSelection() != null
                        ? this.predicate.getFieldSelection()
                        : this.defaultListSelection,
                fieldPredicates,
                this.predicate != null && this.predicate.getGlobalSearchTerm() != null
                        ? this.predicate.getGlobalSearchTerm()
                        : "",
                results,
                fetchBag.size() > queryPageSize,
                page,
                queryPageSize);
    }

    public void addDefaultFilter(final BooleanExpression filter) {
        if (this.defaultFilter != null) {
            this.defaultFilter = this.defaultFilter.and(filter);
        } else {
            this.defaultFilter = filter;
        }
    }
}
