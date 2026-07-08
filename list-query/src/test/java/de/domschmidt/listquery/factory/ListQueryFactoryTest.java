package de.domschmidt.listquery.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import de.domschmidt.listquery.dto.request.ListQuery;
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
                return List.of();
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
