package de.domschmidt.koku.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.dashboard.dto.DashboardViewDto;
import de.domschmidt.formular.dto.FormViewDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.dto.dashboard.containers.grid.DashboardGridContainerDto;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.list.dto.response.ListViewDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CustomerViewContractTest {

    @Test
    void formContainsAllSectionsCrudControlsAndStableAlias() {
        final FormViewDto view = new CustomerController(null, null, null, null).getFormularView();

        assertThat(view.getAlias()).isEqualTo("customer");
        assertThat(view.getRootId()).isNotBlank();
        assertThat(view.getContents()).hasSizeGreaterThanOrEqualTo(25);
        assertThat(view.getPlacements()).hasSize(view.getContents().size() - 1);
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
    }

    @Test
    void listExposesFilterCrudActionsAndSynchronizationContracts() {
        final ListViewDto view = new CustomerController(null, null, null, null).getListView();

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getItemActions()).isNotEmpty();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
    }

    @Test
    void appointmentListExposesFilterCrudActionsAndSynchronizationContracts() {
        final CustomerAppointmentController controller = appointmentController();
        final ListViewDto view = controller.getListView();

        assertThat(view.getItemIdPath()).isEqualTo("id");
        assertThat(view.getFilters()).isNotEmpty();
        assertThat(view.getActions()).isNotEmpty();
        assertThat(view.getRoutedItems()).isNotEmpty();
        assertThat(view.getRoutedContents()).isNotEmpty();
        assertThat(view.getItemClickAction()).isNotNull();
        assertThat(view.getItemActions()).isNotEmpty();
        assertThat(view.getGlobalEventListeners()).isNotEmpty();
        assertThat(view.getGlobalItemStyling()).isNotEmpty();
    }

    @Test
    void appointmentFormExposesNestedBusinessWorkflowWithEmptyReferenceData() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test-user", "n/a"));
        final FormViewDto view;
        try {
            view = appointmentControllerWithEmptyReferenceData().getFormularView();
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertThat(view.getAlias()).isEqualTo("customer-appointment");
        assertThat(view.getRootId()).isNotBlank();
        assertThat(view.getContents()).hasSizeGreaterThan(25);
        assertThat(view.getPlacements()).hasSizeGreaterThan(25);
        assertThat(view.getBusinessRules()).hasSizeGreaterThan(4);
        assertThat(view.getGlobalEventListeners()).hasSizeGreaterThan(4);
    }

    @Test
    void appointmentFormMapsPopulatedReferenceData() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("test-user", "n/a"));
        try {
            final FormViewDto view = appointmentControllerWithReferenceData().getFormularView();

            assertThat(view.getContents()).hasSizeGreaterThan(25);
            assertThat(view.getBusinessRules()).hasSizeGreaterThan(4);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void appointmentFormRejectsMissingAndBlankAuthenticationNames() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> appointmentControllerWithEmptyReferenceData().getFormularView())
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("", "n/a"));
        try {
            assertThatThrownBy(
                            () -> appointmentControllerWithEmptyReferenceData().getFormularView())
                    .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void dashboardDeclaresAllBusinessPanelsAndRevenueChart() {
        final DashboardViewDto view = appointmentController().getDashboardView();

        assertThat(view.getContentRoot()).isInstanceOf(DashboardGridContainerDto.class);
    }

    @Test
    void statisticsExposeValidEmptyStateChartsForExplicitDateRange() {
        final CustomerAppointmentController controller = appointmentControllerWithEmptyReferenceData();
        final YearMonth start = YearMonth.of(2026, java.time.Month.JANUARY);
        final YearMonth end = YearMonth.of(2026, java.time.Month.MARCH);

        assertThat(controller.getAppointmentStatistics(start, end).getSeries()).hasSize(2);
        assertThat(controller.getYearlyVisitsByCustomerId(42L).getSeries()).hasSize(1);
        assertThat(controller.getProductRevenue(start, end).getSeries()).isNotEmpty();
        assertThat(controller.getActivityRevenue(start, end).getSeries()).isNotEmpty();
        assertThat(controller.getCustomerStatistics(start, end).getSeries()).isNotEmpty();
        assertThat(controller.getYearlyRevenueByCustomerId(42L).getSeries()).isNotEmpty();
        assertThat(controller.getRevenuesChartDashboardContent().getSeries()).isNotEmpty();
    }

    @Test
    void customerQueryReturnsAnEmptyPageWithStableMetadata() {
        final EntityManager entityManager = emptyEntityManager();

        assertThat(new CustomerController(entityManager, null, null, null)
                        .findAll(null)
                        .getResults())
                .isEmpty();
    }

    private static CustomerAppointmentController appointmentController() {
        return new CustomerAppointmentController(null, null, null, null, null, null, null, null, null, null);
    }

    private static CustomerAppointmentController appointmentControllerWithEmptyReferenceData() {
        final EntityManager entityManager = emptyEntityManager();
        final Query query = entityManager.createQuery("ignored");
        when(query.getResultList()).thenReturn(Collections.emptyList());

        final ActivityKTableProcessor activities = mock(ActivityKTableProcessor.class);
        final ActivityStepKTableProcessor activitySteps = mock(ActivityStepKTableProcessor.class);
        final ProductKTableProcessor products = mock(ProductKTableProcessor.class);
        final ProductManufacturerKTableProcessor manufacturers = mock(ProductManufacturerKTableProcessor.class);
        final PromotionKTableProcessor promotions = mock(PromotionKTableProcessor.class);
        final UserKTableProcessor users = mock(UserKTableProcessor.class);
        final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto> activityStore =
                emptyStore();
        final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto>
                activityStepStore = emptyStore();
        final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.product.kafka.dto.ProductKafkaDto> productStore =
                emptyStore();
        final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto>
                manufacturerStore = emptyStore();
        final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto> promotionStore =
                emptyStore();
        when(activities.getActivities()).thenReturn(activityStore);
        when(activitySteps.getActivitySteps()).thenReturn(activityStepStore);
        when(products.getProducts()).thenReturn(productStore);
        when(manufacturers.getProductManufacturers()).thenReturn(manufacturerStore);
        when(promotions.getPromotions()).thenReturn(promotionStore);
        when(users.getUsers()).thenReturn(Map.of());

        return new CustomerAppointmentController(
                entityManager, null, null, null, activities, activitySteps, products, manufacturers, promotions, users);
    }

    private static CustomerAppointmentController appointmentControllerWithReferenceData() {
        final EntityManager entityManager = emptyEntityManager();
        final Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstname("Ada");
        customer.setLastname("Lovelace");
        when(entityManager.createQuery(anyString()).getResultList()).thenReturn(java.util.List.of(customer));

        final ActivityKTableProcessor activities = mock(ActivityKTableProcessor.class);
        final ActivityStepKTableProcessor activitySteps = mock(ActivityStepKTableProcessor.class);
        final ProductKTableProcessor products = mock(ProductKTableProcessor.class);
        final ProductManufacturerKTableProcessor manufacturers = mock(ProductManufacturerKTableProcessor.class);
        final PromotionKTableProcessor promotions = mock(PromotionKTableProcessor.class);
        final UserKTableProcessor users = mock(UserKTableProcessor.class);

        final ReadOnlyKeyValueStore<Long, ActivityKafkaDto> activityStore = storeWith(KeyValue.pair(
                10L,
                ActivityKafkaDto.builder().name("Consultation").deleted(false).build()));
        final ReadOnlyKeyValueStore<Long, ActivityStepKafkaDto> activityStepStore = storeWith(KeyValue.pair(
                20L, ActivityStepKafkaDto.builder().name("Clean").deleted(false).build()));
        final ReadOnlyKeyValueStore<Long, ProductKafkaDto> productStore = storeWith(
                KeyValue.pair(
                        30L,
                        ProductKafkaDto.builder()
                                .name("Cream")
                                .manufacturerId(40L)
                                .deleted(false)
                                .build()),
                KeyValue.pair(
                        31L,
                        ProductKafkaDto.builder()
                                .name("Serum")
                                .manufacturerId(41L)
                                .deleted(false)
                                .build()));
        final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> manufacturerStore = storeWith(KeyValue.pair(
                40L,
                ProductManufacturerKafkaDto.builder()
                        .name("Koku")
                        .deleted(false)
                        .build()));
        final ReadOnlyKeyValueStore<Long, PromotionKafkaDto> promotionStore = storeWith(KeyValue.pair(
                50L, PromotionKafkaDto.builder().name("Summer").deleted(false).build()));

        when(activities.getActivities()).thenReturn(activityStore);
        when(activitySteps.getActivitySteps()).thenReturn(activityStepStore);
        when(products.getProducts()).thenReturn(productStore);
        when(manufacturerStore.get(40L))
                .thenReturn(ProductManufacturerKafkaDto.builder()
                        .name("Koku")
                        .deleted(false)
                        .build());
        when(manufacturerStore.get(41L))
                .thenReturn(ProductManufacturerKafkaDto.builder()
                        .name("Other")
                        .deleted(false)
                        .build());
        when(manufacturers.getProductManufacturers()).thenReturn(manufacturerStore);
        when(promotions.getPromotions()).thenReturn(promotionStore);
        when(users.getUsers())
                .thenReturn(Map.of(
                        "test-user",
                        UserKafkaDto.builder()
                                .id("test-user")
                                .firstname("Grace")
                                .lastname("Hopper")
                                .deleted(false)
                                .build()));

        return new CustomerAppointmentController(
                entityManager, null, null, null, activities, activitySteps, products, manufacturers, promotions, users);
    }

    private static EntityManager emptyEntityManager() {
        final EntityManager entityManager = mock(EntityManager.class);
        final EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        final Query query = mock(Query.class, RETURNS_SELF);
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getProperties()).thenReturn(Map.of());
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());
        when(query.getSingleResult()).thenReturn(0L);
        return entityManager;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> ReadOnlyKeyValueStore<K, V> emptyStore() {
        final ReadOnlyKeyValueStore<K, V> store = mock(ReadOnlyKeyValueStore.class);
        final KeyValueIterator<K, V> iterator = mock(KeyValueIterator.class);
        when(iterator.hasNext()).thenReturn(false);
        when(store.all()).thenReturn(iterator);
        return store;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <K, V> ReadOnlyKeyValueStore<K, V> storeWith(final KeyValue<K, V>... entry) {
        final ReadOnlyKeyValueStore<K, V> store = mock(ReadOnlyKeyValueStore.class);
        when(store.all()).thenAnswer(invocation -> new KeyValueIterator<K, V>() {
            private final Iterator<KeyValue<K, V>> entries =
                    java.util.List.of(entry).iterator();

            @Override
            public void close() {
                // The iterator only wraps an in-memory list and owns no closeable resource.
            }

            @Override
            public K peekNextKey() {
                return entry[0].key;
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
}
