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
public class ListQueryFactory<T> {

    private final EntityManager entityManager;
    private final EntityPathBase<T> qClazz;
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
            final EntityPathBase<T> qClazz,
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
        final List<String> requestedSelection = getRequestedSelection();
        final QueryParts queryParts = buildQueryParts();
        final PageSettings pageSettings = getPageSettings();
        final BooleanExpression queryFilterUnion =
                buildQueryFilter(queryParts.filters(), buildGlobalFilters(requestedSelection));
        final List<Tuple> fetchBag = fetchResults(
                queryParts.querySelection(), queryParts.queryOrderSpecifiers(), queryFilterUnion, pageSettings);
        final List<ListItem> results = buildResults(fetchBag, requestedSelection, pageSettings.queryPageSize());
        return new ListPage(
                getResponseSelection(),
                queryParts.fieldPredicates(),
                getGlobalSearchTerm(),
                results,
                fetchBag.size() > pageSettings.queryPageSize(),
                pageSettings.page(),
                pageSettings.queryPageSize());
    }

    private List<String> getRequestedSelection() {
        return this.predicate != null
                        && this.predicate.getFieldSelection() != null
                        && !this.predicate.getFieldSelection().isEmpty()
                ? this.predicate.getFieldSelection()
                : this.defaultListSelection;
    }

    private List<String> getResponseSelection() {
        return this.predicate != null && this.predicate.getFieldSelection() != null
                ? this.predicate.getFieldSelection()
                : this.defaultListSelection;
    }

    private String getGlobalSearchTerm() {
        return this.predicate != null && this.predicate.getGlobalSearchTerm() != null
                ? this.predicate.getGlobalSearchTerm()
                : "";
    }

    private QueryParts buildQueryParts() {
        final Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers = new HashMap<>();
        final List<Expression<?>> querySelection = new ArrayList<>();
        final Map<String, ListFieldQuery> fieldPredicates = new HashMap<>();
        BooleanExpression filters = null;

        for (final Map.Entry<String, Expression<?>> pathQueryEntry : this.expressionQuery.entrySet()) {
            final Expression<?> currentPath = pathQueryEntry.getValue();
            final ListFieldQuery predicateValue = getPredicateValue(pathQueryEntry.getKey(), fieldPredicates);

            querySelection.add(currentPath);
            filters = combineFilters(filters, buildFieldFilter(currentPath, predicateValue));
            addSortOrder(queryOrderSpecifiers, currentPath, predicateValue);
        }

        return new QueryParts(querySelection, fieldPredicates, queryOrderSpecifiers, filters);
    }

    private BooleanExpression buildFieldFilter(final Expression<?> path, final ListFieldQuery predicateValue) {
        final IListFilter iListFilter = FilterResolver.resolveFilter(path.getType());
        return buildFieldFilters(iListFilter, path, predicateValue);
    }

    private void addSortOrder(
            final Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers,
            final Expression<?> path,
            final ListFieldQuery predicateValue) {
        if (predicateValue == null || predicateValue.getSortRanking() == null) {
            return;
        }

        queryOrderSpecifiers.put(
                predicateValue.getSortRanking(),
                orderSpecifier(predicateValue.getSort() == EnumQuerySort.DESC ? Order.DESC : Order.ASC, path));
    }

    private PageSettings getPageSettings() {
        final int queryPageSize = this.predicate != null
                        && this.predicate.getLimit() != null
                        && this.predicate.getLimit() > 0
                        && this.predicate.getLimit() <= 1000
                ? this.predicate.getLimit()
                : 200;
        final int page = this.predicate != null && this.predicate.getPage() != null && this.predicate.getPage() > 0
                ? this.predicate.getPage()
                : 0;
        return new PageSettings(page, queryPageSize, (long) page * queryPageSize);
    }

    private static OrderSpecifier<?> orderSpecifier(final Order order, final Expression<?> path) {
        final Expression<Comparable<?>> orderPath = (Expression<Comparable<?>>) path;
        return new OrderSpecifier<>(order, orderPath);
    }

    private BooleanExpression buildQueryFilter(final BooleanExpression filters, final BooleanExpression globalFilters) {
        return combineFilters(combineFilters(filters, globalFilters), this.defaultFilter);
    }

    private List<Tuple> fetchResults(
            final List<Expression<?>> querySelection,
            final Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers,
            final BooleanExpression queryFilterUnion,
            final PageSettings pageSettings) {
        JPAQuery<?> queryBase = new JPAQuery<>(this.entityManager)
                .select(querySelection.toArray(new Expression<?>[0]))
                .from(this.qClazz)
                .orderBy(buildOrderSpecifiers(queryOrderSpecifiers))
                .where(queryFilterUnion)
                .limit(pageSettings.queryPageSize() + 1L)
                .offset(pageSettings.queryOffset());

        for (final Path<?> join : this.joins) {
            queryBase = queryBase.leftJoin((EntityPath<?>) join);
        }

        return (List<Tuple>) queryBase.fetch();
    }

    private OrderSpecifier<?>[] buildOrderSpecifiers(final Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers) {
        final OrderSpecifier<?>[] orderSpecifiers = queryOrderSpecifiers.keySet().stream()
                .sorted()
                .map(queryOrderSpecifiers::get)
                .toArray(OrderSpecifier[]::new);
        if (orderSpecifiers.length == 0 && this.defaultOrder != null) {
            return new OrderSpecifier<?>[] {this.defaultOrder};
        }
        return orderSpecifiers;
    }

    private List<ListItem> buildResults(
            final List<Tuple> fetchBag, final List<String> requestedSelection, final int queryPageSize) {
        final List<ListItem> results = new ArrayList<>();
        for (int resultCount = 0; resultCount < Math.min(fetchBag.size(), queryPageSize); resultCount++) {
            results.add(buildResultItem(fetchBag.get(resultCount), requestedSelection));
        }
        return results;
    }

    private ListItem buildResultItem(final Tuple currentResult, final List<String> requestedSelection) {
        final Map<String, Object> values = new HashMap<>();
        for (final String currentSelection : requestedSelection) {
            final Expression<?> selectedExpression = this.expressionQuery.get(currentSelection);
            if (selectedExpression != null) {
                values.put(currentSelection, currentResult.get(selectedExpression));
            }
        }

        final Object itemId = currentResult.get(this.itemIdExpression);
        return new ListItem(itemId != null ? itemId.toString() : null, values);
    }

    private record QueryParts(
            List<Expression<?>> querySelection,
            Map<String, ListFieldQuery> fieldPredicates,
            Map<Integer, OrderSpecifier<?>> queryOrderSpecifiers,
            BooleanExpression filters) {}

    private record PageSettings(int page, int queryPageSize, long queryOffset) {}

    private ListFieldQuery getPredicateValue(final String alias, final Map<String, ListFieldQuery> fieldPredicates) {
        if (this.predicate != null
                && this.predicate.getFieldPredicates() != null
                && this.predicate.getFieldPredicates().get(alias) != null) {
            final ListFieldQuery predicateValue =
                    this.predicate.getFieldPredicates().get(alias);
            fieldPredicates.put(alias, predicateValue);
            return predicateValue;
        }
        return null;
    }

    private BooleanExpression buildFieldFilters(
            final IListFilter iListFilter, final Expression<?> path, final ListFieldQuery predicateValue) {
        if (iListFilter == null || predicateValue == null || predicateValue.getPredicates() == null) {
            return null;
        }

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
            fieldFilters = combineFilters(fieldFilters, buildOrGroupFilter(iListFilter, path, group));
        }

        for (final QueryPredicate currentPredicate : andGroup) {
            fieldFilters = combineFilters(fieldFilters, iListFilter.buildSearchExpression(path, currentPredicate));
        }

        return fieldFilters;
    }

    private BooleanExpression buildOrGroupFilter(
            final IListFilter iListFilter, final Expression<?> path, final List<QueryPredicate> group) {
        BooleanExpression orExpression = null;
        for (final QueryPredicate currentPredicate : group) {
            final BooleanExpression filter = iListFilter.buildSearchExpression(path, currentPredicate);
            if (filter != null) {
                orExpression = orExpression == null ? filter : orExpression.or(filter);
            }
        }
        return orExpression;
    }

    private BooleanExpression buildGlobalFilters(final List<String> requestedSelection) {
        if (this.predicate == null
                || this.predicate.getGlobalSearchTerm() == null
                || this.predicate.getGlobalSearchTerm().isEmpty()) {
            return null;
        }

        BooleanExpression globalFilters = null;
        for (final String currentSearchTerm :
                this.predicate.getGlobalSearchTerm().split(" ")) {
            globalFilters = combineFilters(
                    globalFilters, buildGlobalFilterForSearchTerm(currentSearchTerm, requestedSelection));
        }
        return globalFilters;
    }

    private BooleanExpression buildGlobalFilterForSearchTerm(
            final String currentSearchTerm, final List<String> requestedSelection) {
        BooleanExpression currentSearchTermGlobalFilter = null;
        for (final String currentSelection : requestedSelection) {
            currentSearchTermGlobalFilter = combineFilters(
                    currentSearchTermGlobalFilter, buildGlobalFilter(currentSearchTerm, currentSelection));
        }
        return currentSearchTermGlobalFilter;
    }

    private BooleanExpression buildGlobalFilter(final String currentSearchTerm, final String currentSelection) {
        final Expression<?> pathQuery = this.expressionQuery.get(currentSelection);
        if (pathQuery == null) {
            return null;
        }

        final IListFilter iListFilter = FilterResolver.resolveFilter(pathQuery.getType());
        return iListFilter != null ? iListFilter.buildGlobalSearchExpression(pathQuery, currentSearchTerm) : null;
    }

    private BooleanExpression combineFilters(final BooleanExpression existing, final BooleanExpression next) {
        if (next == null) {
            return existing;
        }
        return existing == null ? next : existing.and(next);
    }

    public void addDefaultFilter(final BooleanExpression filter) {
        if (this.defaultFilter != null) {
            this.defaultFilter = this.defaultFilter.and(filter);
        } else {
            this.defaultFilter = filter;
        }
    }
}
