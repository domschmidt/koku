package de.domschmidt.listquery.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import de.domschmidt.listquery.dto.request.EnumQuerySort;
import de.domschmidt.listquery.dto.request.EnumSearchOperator;
import de.domschmidt.listquery.dto.request.ListFieldQuery;
import de.domschmidt.listquery.dto.request.ListQuery;
import de.domschmidt.listquery.dto.request.QueryPredicate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListQueryFactoryTest {

    private CapturingJpaQuery jpaQuery;

    @BeforeEach
    void setUp() {
        this.jpaQuery = new CapturingJpaQuery();
    }

    @Test
    void globalSearchMatchesAnySelectedFieldForOneSearchTerm() {
        createListPage("biga");

        assertThat(capturedQuery()).contains("fullName) like").contains(" or ").doesNotContain(" and lower(address)");
    }

    @Test
    void globalSearchRequiresEverySearchTerm() {
        createListPage("biga berlin");

        assertThat(capturedQuery()).contains(" or ").contains(" and ");
    }

    @Test
    void globalSearchSkipsUnknownSelectedFields() {
        createListPage("biga", List.of("fullName", "missing"));

        assertThat(capturedQuery()).contains("fullName) like").doesNotContain(" or ");
    }

    @Test
    void fieldFiltersCombineAndOrGroupsSortPagingAndDefaults() {
        final ListFieldQuery fieldQuery = new ListFieldQuery();
        fieldQuery.setSort(EnumQuerySort.DESC);
        fieldQuery.setSortRanking(1);
        fieldQuery.setPredicates(List.of(
                QueryPredicate.builder()
                        .searchOperator(EnumSearchOperator.EQ)
                        .searchExpression("Ada")
                        .build(),
                QueryPredicate.builder()
                        .searchOperator(EnumSearchOperator.EQ)
                        .searchExpression("Grace")
                        .orGroupIdentifier("names")
                        .build(),
                QueryPredicate.builder()
                        .searchOperator(EnumSearchOperator.EQ)
                        .searchExpression("Katherine")
                        .orGroupIdentifier("names")
                        .build()));
        final ListQuery predicate = new ListQuery();
        predicate.setFieldPredicates(Map.of("fullName", fieldQuery));
        predicate.setFieldSelection(List.of("fullName", "unknown"));
        predicate.setPage(2);
        predicate.setLimit(25);
        final var fullName = Expressions.stringPath("fullName");
        final ListQueryFactory<Object> factory = new ListQueryFactory<>(
                this.jpaQuery.entityManager(),
                new EntityPathBase<>(Object.class, "customer"),
                Expressions.numberPath(Long.class, "id"),
                predicate);
        factory.addFetchExpr("fullName", fullName);
        factory.addDefaultFilter(fullName.isNotNull());
        factory.addDefaultFilter(fullName.isNotEmpty());

        final var page = factory.create();

        assertThat(page.getFieldPredicates()).containsKey("fullName");
        assertThat(page.getFieldSelection()).containsExactly("fullName", "unknown");
        assertThat(page.getPageIndex()).isEqualTo(2);
        assertThat(page.getPageSize()).isEqualTo(25);
        assertThat(capturedQuery())
                .contains("order by")
                .contains("desc")
                .contains(" or ")
                .contains(" and ");
    }

    @Test
    void invalidPagingFallsBackAndDuplicateAliasesAreRejected() {
        final ListQuery predicate = new ListQuery();
        predicate.setPage(-1);
        predicate.setLimit(1001);
        final ListQueryFactory<Object> factory = new ListQueryFactory<>(
                this.jpaQuery.entityManager(),
                new EntityPathBase<>(Object.class, "customer"),
                Expressions.numberPath(Long.class, "id"),
                predicate);
        factory.addFetchExpr("name", Expressions.stringPath("name"));

        final var page = factory.create();
        final var duplicateExpression = Expressions.stringPath("otherName");

        assertThat(page.getPageIndex()).isZero();
        assertThat(page.getPageSize()).isEqualTo(200);
        assertThatThrownBy(() -> factory.addFetchExpr("name", duplicateExpression))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Duplicate alias");
    }

    @Test
    void mapsRowsJoinsAndUsesDefaultOrdering() {
        final var id = Expressions.numberPath(Long.class, "id");
        final var name = Expressions.stringPath("name");
        final var joined = new EntityPathBase<>(Object.class, "joined");
        this.jpaQuery.results = List.<Object>of(new Object[] {"Ada", 7L}, new Object[] {"Grace", null});
        final ListQuery predicate = new ListQuery();
        predicate.setLimit(1);
        predicate.setFieldSelection(List.of("name", "unknown"));
        final ListQueryFactory<Object> factory = new ListQueryFactory<>(
                this.jpaQuery.entityManager(), new EntityPathBase<>(Object.class, "customer"), id, predicate);
        factory.addFetchExpr("name", name, joined);
        factory.addFetchExpr("id", id);
        factory.setDefaultOrder(new OrderSpecifier<>(Order.ASC, id));

        final var page = factory.create();

        assertThat(page.getResults()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo("7");
            assertThat(item.getValues()).containsEntry("name", "Ada").doesNotContainKey("unknown");
        });
        assertThat(page.getHasMore()).isTrue();
        assertThat(capturedQuery()).contains("left join", "order by");
    }

    @Test
    void explicitAscendingSortOverridesDefaultOrder() {
        final ListFieldQuery fieldQuery = new ListFieldQuery();
        fieldQuery.setSort(EnumQuerySort.ASC);
        fieldQuery.setSortRanking(0);
        final ListQuery predicate = new ListQuery();
        predicate.setFieldPredicates(Map.of("name", fieldQuery));
        final ListQueryFactory<Object> factory = new ListQueryFactory<>(
                this.jpaQuery.entityManager(),
                new EntityPathBase<>(Object.class, "customer"),
                Expressions.numberPath(Long.class, "id"),
                predicate);
        factory.addFetchExpr("name", Expressions.stringPath("name"));

        factory.create();

        assertThat(capturedQuery()).contains("order by", "asc");
    }

    private void createListPage(final String globalSearchTerm) {
        createListPage(globalSearchTerm, null);
    }

    private void createListPage(final String globalSearchTerm, final List<String> fieldSelection) {
        final ListQuery predicate = new ListQuery();
        predicate.setGlobalSearchTerm(globalSearchTerm);
        predicate.setFieldSelection(fieldSelection);
        final ListQueryFactory<Object> factory = new ListQueryFactory<>(
                this.jpaQuery.entityManager(),
                new EntityPathBase<>(Object.class, "customer"),
                Expressions.numberPath(Long.class, "id"),
                predicate);
        factory.addFetchExpr("fullName", Expressions.stringPath("fullName"));
        factory.addFetchExpr("address", Expressions.stringPath("address"));

        factory.create();
    }

    private String capturedQuery() {
        return this.jpaQuery.query();
    }

    private static final class CapturingJpaQuery {

        private final EntityManager entityManager;
        private final EntityManagerFactory entityManagerFactory;
        private final Query query;
        private String queryString;
        private List<?> results = List.of();

        private CapturingJpaQuery() {
            this.query = createProxy(Query.class, this::handleQueryMethod);
            this.entityManagerFactory = createProxy(EntityManagerFactory.class, this::handleEntityManagerFactoryMethod);
            this.entityManager = createProxy(EntityManager.class, this::handleEntityManagerMethod);
        }

        private EntityManager entityManager() {
            return this.entityManager;
        }

        private String query() {
            return this.queryString;
        }

        private Object handleEntityManagerMethod(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return objectMethodValue(proxy, method, args);
            }
            if ("getEntityManagerFactory".equals(method.getName())) {
                return this.entityManagerFactory;
            }
            if ("createQuery".equals(method.getName()) && args != null && args.length > 0) {
                this.queryString = (String) args[0];
                return this.query;
            }
            return defaultValue(method.getReturnType());
        }

        private Object handleEntityManagerFactoryMethod(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return objectMethodValue(proxy, method, args);
            }
            if ("getProperties".equals(method.getName())) {
                return Map.of();
            }
            return defaultValue(method.getReturnType());
        }

        private Object handleQueryMethod(final Object proxy, final Method method, final Object[] args) {
            if (isObjectMethod(method)) {
                return objectMethodValue(proxy, method, args);
            }
            if ("getResultList".equals(method.getName())) {
                return this.results;
            }
            if ("getParameters".equals(method.getName())) {
                return Set.of();
            }
            if (Query.class.isAssignableFrom(method.getReturnType())) {
                return this.query;
            }
            return defaultValue(method.getReturnType());
        }

        private static <T> T createProxy(final Class<T> type, final InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
        }

        private static boolean isObjectMethod(final Method method) {
            return method.getDeclaringClass() == Object.class;
        }

        private static Object objectMethodValue(final Object proxy, final Method method, final Object[] args) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> proxy.getClass().getInterfaces()[0].getSimpleName() + " proxy";
                default -> null;
            };
        }

        private static Object defaultValue(final Class<?> returnType) {
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0F;
            }
            if (returnType == double.class) {
                return 0D;
            }
            return 0;
        }
    }
}
