package de.domschmidt.koku.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentActivity;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;

class CustomerAppointmentDashboardTest {

    @Test
    void appointmentPanelShowsCompletedAndOpenAppointments() {
        final Query query = queryReturningSingleResults(10L, 4L, "6");

        final var panel = controller(query).getAppointmentDashboardContent();

        assertThat(panel.getHeadline()).isEqualTo("10");
        assertThat(panel.getProgress()).isEqualTo((short) 40);
        assertThat(panel.getProgressDetails()).hasSize(2);
    }

    @Test
    void appointmentPanelHandlesMonthWithoutAppointments() {
        final Query query = queryReturningSingleResults(0L, 0L, "0");

        assertThat(controller(query).getAppointmentDashboardContent().getProgress())
                .isZero();
    }

    @Test
    void currentRevenuePanelCalculatesProgressAndComparisons() {
        final Query query = queryReturningSingleResults(
                new BigDecimal("200"), new BigDecimal("125"), new BigDecimal("100"), new BigDecimal("250"));

        final var panel = controller(query).getRevenuesCurrentDashboardContent();

        assertThat(panel.getProgress()).isEqualTo((short) 63);
        assertThat(panel.getProgressDetails()).extracting("headline").containsExactly("+100%", "-20%");
    }

    @Test
    void currentRevenuePanelHandlesZeroRevenue() {
        final Query query =
                queryReturningSingleResults(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        final var panel = controller(query).getRevenuesCurrentDashboardContent();

        assertThat(panel.getProgress()).isZero();
        assertThat(panel.getProgressDetails()).extracting("headline").containsOnly("?%");
    }

    @Test
    void revenuePreviewShowsPlannedMonths() {
        final Query query =
                queryReturningSingleResults(new BigDecimal("450"), new BigDecimal("100"), new BigDecimal("150"));

        final var panel = controller(query).getRevenuesPreviewDashboardContent();

        assertThat(panel.getHeadline()).contains("450");
        assertThat(panel.getExplanations()).hasSize(2);
    }

    @Test
    void emptyTopPanelsAndNewCustomerPanelHaveStableFallbacks() {
        final Query query = queryReturningResultList(Collections.emptyList());
        final CustomerAppointmentController controller = controller(query);

        assertThat(controller.getTopProductDashboardContent().getHeadline()).isEqualTo("?");
        assertThat(controller.getTopActivityDashboardContent().getHeadline()).isEqualTo("?");
        assertThat(controller.getTopCustomerDashboardContent().getHeadline()).contains("?");

        when(query.getSingleResult()).thenReturn(3L);
        assertThat(controller.getNewCustomerDashboardContent().getHeadline()).isEqualTo("3");
    }

    @Test
    void topPanelsCountRepeatedProductsAndActivities() {
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setSoldProducts(java.util.List.of(
                new CustomerAppointmentSoldProduct(appointment, 10L, BigDecimal.ONE, 0),
                new CustomerAppointmentSoldProduct(appointment, 10L, BigDecimal.ONE, 1)));
        appointment.setActivities(java.util.List.of(
                new CustomerAppointmentActivity(appointment, 20L, BigDecimal.ONE, 0),
                new CustomerAppointmentActivity(appointment, 20L, BigDecimal.ONE, 1)));
        final Query query = queryReturningResultList(java.util.List.of(appointment));
        final ActivityKTableProcessor activities = mock(ActivityKTableProcessor.class);
        final ProductKTableProcessor products = mock(ProductKTableProcessor.class);
        final ReadOnlyKeyValueStore<Long, ActivityKafkaDto> activityStore = mock(ReadOnlyKeyValueStore.class);
        final ReadOnlyKeyValueStore<Long, ProductKafkaDto> productStore = mock(ReadOnlyKeyValueStore.class);
        when(activities.getActivities()).thenReturn(activityStore);
        when(products.getProducts()).thenReturn(productStore);
        when(activityStore.get(20L))
                .thenReturn(ActivityKafkaDto.builder().name("Facial").build());
        when(productStore.get(10L))
                .thenReturn(ProductKafkaDto.builder().name("Cream").build());
        final CustomerAppointmentController controller = new CustomerAppointmentController(
                entityManager(query), null, null, null, activities, null, products, null, null, null);

        assertThat(controller.getTopProductDashboardContent().getHeadline()).isEqualTo("Cream");
        assertThat(controller.getTopActivityDashboardContent().getHeadline()).isEqualTo("Facial");
    }

    @Test
    void appointmentQuerySupportsGlobalAndCustomerScopedEmptyResults() {
        final Query query = queryReturningResultList(Collections.emptyList());
        when(query.getSingleResult()).thenReturn(0L);
        final CustomerAppointmentController controller = controller(query);

        assertThat(controller.findAll(null, null).getResults()).isEmpty();
        assertThat(controller.findAll(42L, null).getResults()).isEmpty();
    }

    @Test
    void percentageDifferenceFormatsPositiveNegativeAndEqualValues() {
        assertThat(CustomerAppointmentController.calculatedFormattedDifference(
                        new BigDecimal("125"), new BigDecimal("100")))
                .isEqualTo("+25%");
        assertThat(CustomerAppointmentController.calculatedFormattedDifference(
                        new BigDecimal("75"), new BigDecimal("100")))
                .isEqualTo("-25%");
        assertThat(CustomerAppointmentController.calculatedFormattedDifference(
                        new BigDecimal("100"), new BigDecimal("100")))
                .isEqualTo("0%");
    }

    @Test
    void statisticsAndDashboardChartsRemainStableWithoutAppointments() {
        final Query query = queryReturningResultList(Collections.emptyList());
        when(query.getSingleResult()).thenReturn(0L);
        final CustomerAppointmentController controller = statisticsController(query);
        final YearMonth start = YearMonth.of(2025, java.time.Month.JANUARY);
        final YearMonth end = YearMonth.of(2026, java.time.Month.JUNE);

        assertThat(controller.getAppointmentStatistics(start, end).getSeries()).isNotEmpty();
        assertThat(controller.getAppointmentStatistics(null, null).getSeries()).isNotEmpty();
        assertThat(controller.getYearlyVisitsByCustomerId(42L).getSeries()).isNotEmpty();
        assertThat(controller.getProductRevenue(start, end).getSeries()).hasSize(2);
        assertThat(controller.getProductRevenue(null, null).getSeries()).hasSize(2);
        assertThat(controller.getActivityRevenue(start, end).getSeries()).hasSize(2);
        assertThat(controller.getActivityRevenue(null, null).getSeries()).hasSize(2);
        assertThat(controller.getCustomerStatistics(start, end).getSeries()).hasSize(3);
        assertThat(controller.getCustomerStatistics(null, null).getSeries()).hasSize(3);
        assertThat(controller.getYearlyRevenueByCustomerId(42L).getSeries()).hasSize(2);
        assertThat(controller.getRevenuesChartDashboardContent().getSeries()).hasSize(2);
        assertThat(controller.getDashboardView().getContentRoot()).isNotNull();
    }

    @Test
    void productAndActivityRevenueChartsMapNonEmptySales() {
        final CustomerAppointment appointment = new CustomerAppointment();
        final CustomerAppointmentSoldProduct soldProduct =
                new CustomerAppointmentSoldProduct(appointment, 10L, BigDecimal.ONE, 0);
        soldProduct.setFinalPriceSnapshot(new BigDecimal("12.00"));
        final CustomerAppointmentActivity activity =
                new CustomerAppointmentActivity(appointment, 20L, BigDecimal.ONE, 0);
        activity.setFinalPriceSnapshot(new BigDecimal("34.00"));
        appointment.setSoldProducts(java.util.List.of(soldProduct));
        appointment.setActivities(java.util.List.of(activity));
        final Query query = queryReturningResultList(java.util.List.of(appointment));
        final ActivityKTableProcessor activities = mock(ActivityKTableProcessor.class);
        final ProductKTableProcessor products = mock(ProductKTableProcessor.class);
        final ProductManufacturerKTableProcessor manufacturers = mock(ProductManufacturerKTableProcessor.class);
        final ReadOnlyKeyValueStore<Long, ActivityKafkaDto> activityStore = storeWith(
                KeyValue.pair(20L, ActivityKafkaDto.builder().name("Facial").build()));
        final ReadOnlyKeyValueStore<Long, ProductKafkaDto> productStore = storeWith(KeyValue.pair(
                10L, ProductKafkaDto.builder().name("Cream").manufacturerId(30L).build()));
        when(activities.getActivities()).thenReturn(activityStore);
        when(products.getProducts()).thenReturn(productStore);
        final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> manufacturerStore =
                mock(ReadOnlyKeyValueStore.class);
        when(manufacturerStore.get(30L))
                .thenReturn(ProductManufacturerKafkaDto.builder().name("Koku").build());
        when(manufacturers.getProductManufacturers()).thenReturn(manufacturerStore);
        final CustomerAppointmentController controller = new CustomerAppointmentController(
                entityManager(query), null, null, null, activities, null, products, manufacturers, null, null);

        assertThat(controller
                        .getProductRevenue(
                                YearMonth.of(2026, java.time.Month.JANUARY),
                                YearMonth.of(2026, java.time.Month.DECEMBER))
                        .getSeries())
                .hasSize(2);
        assertThat(controller
                        .getActivityRevenue(
                                YearMonth.of(2026, java.time.Month.JANUARY),
                                YearMonth.of(2026, java.time.Month.DECEMBER))
                        .getSeries())
                .hasSize(2);
    }

    @Test
    void groupedStatisticsMapNonEmptyJpaResults() {
        final YearMonth month = YearMonth.of(2026, java.time.Month.JANUARY);
        final Query appointmentQuery = queryReturningResultLists(
                Collections.singletonList(new Object[] {"202601", new BigDecimal("10"), new BigDecimal("5")}));
        assertThat(controller(appointmentQuery)
                        .getAppointmentStatistics(month, month)
                        .getSeries())
                .hasSize(2);

        final Query visitsQuery = queryReturningResultLists(Collections.singletonList(new Object[] {2L, 2026}));
        assertThat(controller(visitsQuery).getYearlyVisitsByCustomerId(42L).getSeries())
                .hasSize(1);

        final Query yearlyRevenueQuery = queryReturningResultLists(
                Collections.singletonList(new Object[] {2026, new BigDecimal("10"), new BigDecimal("5")}));
        assertThat(controller(yearlyRevenueQuery)
                        .getYearlyRevenueByCustomerId(42L)
                        .getSeries())
                .hasSize(2);

        final Query chartQuery =
                queryReturningResultLists(Collections.singletonList(new Object[] {"202601", new BigDecimal("15")}));
        assertThat(controller(chartQuery).getRevenuesChartDashboardContent().getSeries())
                .hasSize(2);
    }

    @Test
    void customerStatisticsAndTopCustomerMapNonEmptyJpaResults() {
        final de.domschmidt.koku.customer.persistence.Customer customer =
                new de.domschmidt.koku.customer.persistence.Customer();
        customer.setFirstname("Ada");
        customer.setLastname("");
        final Query statisticsQuery = queryReturningResultLists(
                Collections.singletonList(new Object[] {customer, new BigDecimal("5"), new BigDecimal("10")}),
                Collections.singletonList(new Object[] {customer, 2L}));
        assertThat(controller(statisticsQuery)
                        .getCustomerStatistics(
                                YearMonth.of(2026, java.time.Month.JANUARY),
                                YearMonth.of(2026, java.time.Month.JANUARY))
                        .getSeries())
                .hasSize(3);

        final Query topCustomerQuery =
                queryReturningResultLists(Collections.singletonList(new Object[] {customer, new BigDecimal("15")}));
        assertThat(controller(topCustomerQuery).getTopCustomerDashboardContent().getTopHeadline())
                .isEqualTo("Ada");
    }

    private static CustomerAppointmentController controller(Query query) {
        return new CustomerAppointmentController(
                entityManager(query),
                null,
                null,
                null,
                mock(ActivityKTableProcessor.class),
                null,
                mock(ProductKTableProcessor.class),
                null,
                null,
                null);
    }

    private static EntityManager entityManager(Query query) {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        return entityManager;
    }

    private static CustomerAppointmentController statisticsController(Query query) {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        final ActivityKTableProcessor activities = mock(ActivityKTableProcessor.class);
        final ProductKTableProcessor products = mock(ProductKTableProcessor.class);
        final ReadOnlyKeyValueStore<Long, ?> activityStore = emptyStore();
        final ReadOnlyKeyValueStore<Long, ?> productStore = emptyStore();
        when(activities.getActivities()).thenReturn((ReadOnlyKeyValueStore) activityStore);
        when(products.getProducts()).thenReturn((ReadOnlyKeyValueStore) productStore);
        return new CustomerAppointmentController(
                entityManager, null, null, null, activities, null, products, null, null, null);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> ReadOnlyKeyValueStore<K, V> emptyStore() {
        final ReadOnlyKeyValueStore<K, V> store = mock(ReadOnlyKeyValueStore.class);
        when(store.all()).thenAnswer(invocation -> {
            final KeyValueIterator<K, V> iterator = mock(KeyValueIterator.class);
            when(iterator.hasNext()).thenReturn(false);
            return iterator;
        });
        return store;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> ReadOnlyKeyValueStore<K, V> storeWith(KeyValue<K, V> entry) {
        final ReadOnlyKeyValueStore<K, V> store = mock(ReadOnlyKeyValueStore.class);
        when(store.all()).thenAnswer(invocation -> new KeyValueIterator<K, V>() {
            private final java.util.Iterator<KeyValue<K, V>> entries =
                    java.util.List.of(entry).iterator();

            @Override
            public void close() {
                // The iterator only wraps an in-memory list and owns no closeable resource.
            }

            @Override
            public K peekNextKey() {
                return entry.key;
            }

            @Override
            public boolean hasNext() {
                return entries.hasNext();
            }

            @Override
            public KeyValue<K, V> next() {
                return entries.next();
            }
        });
        return store;
    }

    private static Query queryReturningSingleResults(Object first, Object... remaining) {
        final Query query = mock(Query.class, RETURNS_SELF);
        when(query.getSingleResult()).thenReturn(first, remaining);
        return query;
    }

    private static Query queryReturningResultList(Object result) {
        final Query query = mock(Query.class, RETURNS_SELF);
        when(query.getResultList()).thenReturn((java.util.List) result);
        return query;
    }

    private static Query queryReturningResultLists(Object first, Object... remaining) {
        final Query query = mock(Query.class, RETURNS_SELF);
        final java.util.List<Object> results = new java.util.ArrayList<>();
        results.add(first);
        results.addAll(java.util.List.of(remaining));
        final java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger();
        when(query.getResultList()).thenAnswer(invocation -> results.get(index.getAndIncrement()));
        return query;
    }
}
