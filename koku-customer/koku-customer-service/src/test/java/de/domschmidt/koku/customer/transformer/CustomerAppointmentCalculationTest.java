package de.domschmidt.koku.customer.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityPriceHistoryKafkaDto;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentActivityDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentPromotionDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentSoldProductDomain;
import de.domschmidt.koku.customer.exceptions.ActivityIdNotFoundException;
import de.domschmidt.koku.customer.exceptions.ActivityStepIdNotFoundException;
import de.domschmidt.koku.customer.exceptions.ProductIdNotFoundException;
import de.domschmidt.koku.customer.exceptions.PromotionIdNotFoundException;
import de.domschmidt.koku.customer.exceptions.UserIdNotFoundException;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.kafka.users.service.UserKTableProcessor;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import de.domschmidt.koku.dto.customer.KokuActivityPriceSummaryRequestDto;
import de.domschmidt.koku.dto.customer.KokuActivitySoldProductSummaryRequestDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentActivityDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentActivityStepTreatmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentOverallPriceSummaryRequestDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentProductTreatmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentPromotionDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentSoldProductDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentTreatmentDto;
import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductPriceHistoryKafkaDto;
import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerAppointmentCalculationTest {

    private final ActivityKTableProcessor activityProcessor = mock(ActivityKTableProcessor.class);
    private final ProductKTableProcessor productProcessor = mock(ProductKTableProcessor.class);
    private final PromotionKTableProcessor promotionProcessor = mock(PromotionKTableProcessor.class);
    private final ActivityStepKTableProcessor activityStepProcessor = mock(ActivityStepKTableProcessor.class);
    private final ProductManufacturerKTableProcessor manufacturerProcessor =
            mock(ProductManufacturerKTableProcessor.class);
    private final UserKTableProcessor userProcessor = mock(UserKTableProcessor.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ReadOnlyKeyValueStore<Long, ActivityKafkaDto> activities = mock(ReadOnlyKeyValueStore.class);
    private final ReadOnlyKeyValueStore<Long, ProductKafkaDto> products = mock(ReadOnlyKeyValueStore.class);
    private final ReadOnlyKeyValueStore<Long, PromotionKafkaDto> promotions = mock(ReadOnlyKeyValueStore.class);
    private final ReadOnlyKeyValueStore<Long, de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto>
            activitySteps = mock(ReadOnlyKeyValueStore.class);
    private final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> manufacturers =
            mock(ReadOnlyKeyValueStore.class);
    private CustomerAppointmentToCustomerAppointmentDtoTransformer transformer;

    @BeforeEach
    void setUp() {
        when(activityProcessor.getActivities()).thenReturn(activities);
        when(productProcessor.getProducts()).thenReturn(products);
        when(promotionProcessor.getPromotions()).thenReturn(promotions);
        when(activityStepProcessor.getActivitySteps()).thenReturn(activitySteps);
        when(manufacturerProcessor.getProductManufacturers()).thenReturn(manufacturers);
        transformer = new CustomerAppointmentToCustomerAppointmentDtoTransformer(
                entityManager,
                activityProcessor,
                activityStepProcessor,
                productProcessor,
                manufacturerProcessor,
                promotionProcessor,
                userProcessor);
    }

    @Test
    void activityPriceUsesHistoryThenItemAndOverallDiscounts() {
        final LocalDateTime appointmentDate = LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 0);
        when(activities.get(1L))
                .thenReturn(ActivityKafkaDto.builder()
                        .priceHistory(List.of(
                                ActivityPriceHistoryKafkaDto.builder()
                                        .price(new BigDecimal("50.00"))
                                        .recorded(appointmentDate.minusMonths(2))
                                        .build(),
                                ActivityPriceHistoryKafkaDto.builder()
                                        .price(new BigDecimal("100.00"))
                                        .recorded(appointmentDate.minusDays(1))
                                        .build()))
                        .build());
        when(promotions.get(7L))
                .thenReturn(PromotionKafkaDto.builder()
                        .activityAbsoluteItemSavings(new BigDecimal("10.00"))
                        .activityRelativeItemSavings(new BigDecimal("10.00"))
                        .activityAbsoluteSavings(new BigDecimal("5.00"))
                        .activityRelativeSavings(new BigDecimal("25.00"))
                        .build());

        final BigDecimal result = transformer.calculateCustomerAppointmentActivityPriceSum(
                appointmentDate,
                List.of(new KokuCustomerAppointmentActivityDomain(1L, null)),
                List.of(new KokuCustomerAppointmentPromotionDomain(7L)));

        assertThat(result).isEqualByComparingTo("57.00");
    }

    @Test
    void explicitActivityPriceIsUsedAndOverallDiscountCannotProduceNegativeTotal() {
        when(promotions.get(7L))
                .thenReturn(PromotionKafkaDto.builder()
                        .activityAbsoluteSavings(new BigDecimal("100.00"))
                        .build());

        final BigDecimal result = transformer.calculateCustomerAppointmentActivityPriceSum(
                LocalDateTime.now(),
                List.of(new KokuCustomerAppointmentActivityDomain(1L, new BigDecimal("20.00"))),
                List.of(new KokuCustomerAppointmentPromotionDomain(7L)));

        assertThat(result).isEqualByComparingTo("0.00");
    }

    @Test
    void productPriceUsesLatestHistoricalPriceAndAllDiscountLevels() {
        final LocalDateTime appointmentDate = LocalDateTime.of(2026, java.time.Month.JULY, 12, 10, 0);
        when(products.get(2L))
                .thenReturn(ProductKafkaDto.builder()
                        .priceHistory(List.of(ProductPriceHistoryKafkaDto.builder()
                                .price(new BigDecimal("80.00"))
                                .recorded(appointmentDate.minusDays(1))
                                .build()))
                        .build());
        when(promotions.get(8L))
                .thenReturn(PromotionKafkaDto.builder()
                        .productAbsoluteItemSavings(new BigDecimal("10.00"))
                        .productRelativeItemSavings(new BigDecimal("10.00"))
                        .productAbsoluteSavings(new BigDecimal("3.00"))
                        .productRelativeSavings(new BigDecimal("50.00"))
                        .build());

        final BigDecimal result = transformer.calculateCustomerAppointmentSoldProductPriceSum(
                appointmentDate,
                List.of(new KokuCustomerAppointmentSoldProductDomain(2L, null)),
                List.of(new KokuCustomerAppointmentPromotionDomain(8L)));

        assertThat(result).isEqualByComparingTo("30.00");
    }

    @Test
    void missingPriceHistoryDefaultsToZero() {
        when(products.get(2L)).thenReturn(ProductKafkaDto.builder().build());

        assertThat(transformer.calculateSoldProductPrice(
                        LocalDateTime.now(), new KokuCustomerAppointmentSoldProductDomain(2L, null), List.of()))
                .isEqualByComparingTo("0.00");
    }

    @Test
    void activityDurationsDriveHumanReadableSummaryAndAppointmentEnd() {
        when(activities.get(1L))
                .thenReturn(ActivityKafkaDto.builder()
                        .approximatelyDuration(Duration.ofDays(1).plusHours(2).plusMinutes(15))
                        .build());
        when(activities.get(2L))
                .thenReturn(ActivityKafkaDto.builder()
                        .approximatelyDuration(Duration.ofMinutes(45))
                        .build());
        final LocalDateTime start = LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0);

        assertThat(transformer.calculateCustomerAppointmentActivityDurationHumanReadable(List.of(1L, 2L)))
                .isEqualTo("1 Tage 3 Std.");
        assertThat(transformer.calculateCustomerAppointmentEnd(
                        start,
                        List.of(
                                new KokuCustomerAppointmentActivityDomain(1L, null),
                                new KokuCustomerAppointmentActivityDomain(2L, null))))
                .isEqualTo(LocalDateTime.of(2026, java.time.Month.JULY, 13, 12, 0));
    }

    @Test
    void activitySummaryPreservesTreatmentOrder() {
        when(activities.get(1L))
                .thenReturn(ActivityKafkaDto.builder().name("Cut").build());
        when(activities.get(2L))
                .thenReturn(ActivityKafkaDto.builder().name("Color").build());

        assertThat(transformer.calculateCustomerAppointmentActivitySummary(List.of(
                        new KokuCustomerAppointmentActivityDomain(1L, null),
                        new KokuCustomerAppointmentActivityDomain(2L, null))))
                .isEqualTo("Cut, Color");
    }

    @Test
    void priceSummariesSupportEveryDateAndTimeCombination() {
        final LocalDate date = LocalDate.of(2026, java.time.Month.JULY, 12);
        final LocalTime time = LocalTime.of(9, 30);

        assertThat(transformer
                        .transformToActivityPriceSummary(KokuActivityPriceSummaryRequestDto.builder()
                                .date(date)
                                .time(time)
                                .build())
                        .getPriceSum())
                .isEqualTo("0,00");
        assertThat(transformer
                        .transformToActivityPriceSummary(KokuActivityPriceSummaryRequestDto.builder()
                                .date(date)
                                .build())
                        .getDurationSum())
                .isEmpty();
        transformer.transformToActivityPriceSummary(
                KokuActivityPriceSummaryRequestDto.builder().time(time).build());
        transformer.transformToActivityPriceSummary(
                KokuActivityPriceSummaryRequestDto.builder().build());

        assertThat(transformer
                        .transformToOverallPriceSummary(KokuCustomerAppointmentOverallPriceSummaryRequestDto.builder()
                                .date(date)
                                .time(time)
                                .build())
                        .getPriceSum())
                .isEqualTo("0,00");
        transformer.transformToOverallPriceSummary(KokuCustomerAppointmentOverallPriceSummaryRequestDto.builder()
                .date(date)
                .build());
        transformer.transformToOverallPriceSummary(KokuCustomerAppointmentOverallPriceSummaryRequestDto.builder()
                .time(time)
                .build());
        transformer.transformToOverallPriceSummary(
                KokuCustomerAppointmentOverallPriceSummaryRequestDto.builder().build());

        assertThat(transformer
                        .transformToSoldProductPriceSummary(KokuActivitySoldProductSummaryRequestDto.builder()
                                .date(date)
                                .time(time)
                                .build())
                        .getPriceSum())
                .isEqualTo("0,00");
        transformer.transformToSoldProductPriceSummary(
                KokuActivitySoldProductSummaryRequestDto.builder().date(date).build());
        transformer.transformToSoldProductPriceSummary(
                KokuActivitySoldProductSummaryRequestDto.builder().time(time).build());
        transformer.transformToSoldProductPriceSummary(
                KokuActivitySoldProductSummaryRequestDto.builder().build());
    }

    @Test
    void summaryContainsAppointmentIdentityAndTimestamp() {
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setId(42L);
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 30));

        final var summary = transformer.transformToSummaryDto(appointment);

        assertThat(summary.getId()).isEqualTo(42L);
        assertThat(summary.getAppointmentSummary()).contains("12.07.2026").contains("09:30");
    }

    @Test
    void entityUpdateRejectsUnknownKafkaReferences() {
        when(userProcessor.getUsers()).thenReturn(java.util.Map.of());
        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder().userId("missing").build()))
                .isInstanceOf(UserIdNotFoundException.class);

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder()
                                .activities(List.of(KokuCustomerAppointmentActivityDto.builder()
                                        .activityId(99L)
                                        .build()))
                                .build()))
                .isInstanceOf(ActivityIdNotFoundException.class);

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder()
                                .treatmentSequence(List.of(KokuCustomerAppointmentActivityStepTreatmentDto.builder()
                                        .activityStepId(99L)
                                        .build()))
                                .build()))
                .isInstanceOf(ActivityStepIdNotFoundException.class);

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder()
                                .treatmentSequence(List.of(KokuCustomerAppointmentProductTreatmentDto.builder()
                                        .productId(99L)
                                        .build()))
                                .build()))
                .isInstanceOf(ProductIdNotFoundException.class);

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder()
                                .soldProducts(List.of(KokuCustomerAppointmentSoldProductDto.builder()
                                        .productId(99L)
                                        .build()))
                                .build()))
                .isInstanceOf(ProductIdNotFoundException.class);

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new CustomerAppointment(),
                        KokuCustomerAppointmentDto.builder()
                                .promotions(List.of(KokuCustomerAppointmentPromotionDto.builder()
                                        .promotionId(99L)
                                        .build()))
                                .build()))
                .isInstanceOf(PromotionIdNotFoundException.class);
    }

    @Test
    void entityUpdateReplacesNestedContentAndRefreshesSnapshots() throws Exception {
        final LocalDateTime start = LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0);
        final Customer customer = new Customer();
        customer.setId(42L);
        customer.setFirstname("Ada");
        customer.setLastname("Lovelace");
        when(entityManager.getReference(Customer.class, 42L)).thenReturn(customer);
        when(userProcessor.getUsers())
                .thenReturn(java.util.Map.of("u-1", new de.domschmidt.koku.user.kafka.dto.UserKafkaDto()));
        when(activities.get(1L))
                .thenReturn(ActivityKafkaDto.builder()
                        .name("Cut")
                        .approximatelyDuration(Duration.ofMinutes(45))
                        .build());
        when(activitySteps.get(3L))
                .thenReturn(de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto.builder()
                        .name("Wash")
                        .build());
        when(products.get(2L))
                .thenReturn(ProductKafkaDto.builder()
                        .name("Serum")
                        .manufacturerId(4L)
                        .build());
        when(manufacturers.get(4L))
                .thenReturn(ProductManufacturerKafkaDto.builder().name("Maker").build());
        when(promotions.get(7L))
                .thenReturn(PromotionKafkaDto.builder().name("Summer").build());
        final CustomerAppointment appointment = new CustomerAppointment();
        final KokuCustomerAppointmentDto update = KokuCustomerAppointmentDto.builder()
                .date(start.toLocalDate())
                .time(start.toLocalTime())
                .description("Visit")
                .additionalInfo("Notes")
                .customerId(42L)
                .userId("u-1")
                .activities(List.of(KokuCustomerAppointmentActivityDto.builder()
                        .activityId(1L)
                        .price(new BigDecimal("50.00"))
                        .build()))
                .treatmentSequence(List.of(
                        KokuCustomerAppointmentActivityStepTreatmentDto.builder()
                                .activityStepId(3L)
                                .build(),
                        KokuCustomerAppointmentProductTreatmentDto.builder()
                                .productId(2L)
                                .build()))
                .soldProducts(List.of(KokuCustomerAppointmentSoldProductDto.builder()
                        .productId(2L)
                        .price(new BigDecimal("20.00"))
                        .build()))
                .promotions(List.of(KokuCustomerAppointmentPromotionDto.builder()
                        .promotionId(7L)
                        .build()))
                .deleted(true)
                .build();

        transformer.transformToEntity(appointment, update);

        assertThat(appointment.getStart()).isEqualTo(start);
        assertThat(appointment.getCustomer()).isSameAs(customer);
        assertThat(appointment.getUserId()).isEqualTo("u-1");
        assertThat(appointment.getActivities()).hasSize(1);
        assertThat(appointment.getTreatmentSequence()).hasSize(2);
        assertThat(appointment.getSoldProducts()).hasSize(1);
        assertThat(appointment.getPromotions()).hasSize(1);
        assertThat(appointment.getActivitiesRevenueSnapshot()).isEqualByComparingTo("50.00");
        assertThat(appointment.getSoldProductsRevenueSnapshot()).isEqualByComparingTo("20.00");
        assertThat(appointment.getActivitiesSummarySnapshot()).isEqualTo("Cut");
        assertThat(appointment.getSoldProductsSummarySnapshot()).isEqualTo("Maker / Serum");
        assertThat(appointment.getCalculatedEndSnapshot()).isEqualTo(start.plusMinutes(45));
        assertThat(appointment.isDeleted()).isTrue();

        final KokuCustomerAppointmentDto roundTrip = transformer.transformToDto(appointment);
        assertThat(roundTrip.getCustomerId()).isEqualTo(42L);
        assertThat(roundTrip.getCustomerName()).isNotBlank();
        assertThat(roundTrip.getActivities())
                .singleElement()
                .extracting("activityId")
                .isEqualTo(1L);
        assertThat(roundTrip.getTreatmentSequence()).hasSize(2);
        assertThat(roundTrip.getSoldProducts())
                .singleElement()
                .extracting("productId")
                .isEqualTo(2L);
        assertThat(roundTrip.getPromotions())
                .singleElement()
                .extracting("promotionId")
                .isEqualTo(7L);
        assertThat(roundTrip.getActivityDurationSummary()).isEqualTo("45 Min.");
        assertThat(roundTrip.getOverallPriceSummary()).isNotBlank();
    }

    @Test
    void nullableCollectionsAndCalculatedEndProduceAnEmptyRoundTrip() {
        final Customer customer = new Customer();
        customer.setId(42L);
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setCustomer(customer);
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0));
        appointment.setActivities(null);
        appointment.setTreatmentSequence(null);
        appointment.setSoldProducts(null);
        appointment.setPromotions(null);

        final KokuCustomerAppointmentDto dto = transformer.transformToDto(appointment);

        assertThat(dto.getActivities()).isEmpty();
        assertThat(dto.getTreatmentSequence()).isEmpty();
        assertThat(dto.getSoldProducts()).isEmpty();
        assertThat(dto.getPromotions()).isEmpty();
        assertThat(dto.getApproximatelyEndDate()).isNull();
        assertThat(dto.getApproximatelyEndTime()).isNull();
    }

    @Test
    void nullUpdatesLeaveCollectionsUnchangedAndUnexpectedTreatmentsAreRejected() throws Exception {
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0));
        appointment.setActivities(new java.util.ArrayList<>());
        appointment.setTreatmentSequence(new java.util.ArrayList<>());
        appointment.setSoldProducts(new java.util.ArrayList<>());
        appointment.setPromotions(new java.util.ArrayList<>());

        assertThat(transformer.transformToEntity(
                        appointment, KokuCustomerAppointmentDto.builder().build()))
                .isSameAs(appointment);
        assertThatThrownBy(() -> transformer.transformToEntity(
                        appointment,
                        KokuCustomerAppointmentDto.builder()
                                .treatmentSequence(List.of(mock(KokuCustomerAppointmentTreatmentDto.class)))
                                .build()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void pricesClampNegativeExplicitValuesAndUseEarliestFutureHistory() {
        final LocalDateTime appointmentDate = LocalDateTime.of(2026, java.time.Month.JANUARY, 1, 10, 0);
        when(activities.get(1L))
                .thenReturn(ActivityKafkaDto.builder()
                        .priceHistory(List.of(ActivityPriceHistoryKafkaDto.builder()
                                .price(new BigDecimal("30.00"))
                                .recorded(appointmentDate.plusDays(1))
                                .build()))
                        .build());
        when(products.get(2L))
                .thenReturn(ProductKafkaDto.builder()
                        .priceHistory(List.of(ProductPriceHistoryKafkaDto.builder()
                                .price(new BigDecimal("40.00"))
                                .recorded(appointmentDate.plusDays(1))
                                .build()))
                        .build());

        assertThat(transformer.calculateActivityPrice(
                        appointmentDate, new KokuCustomerAppointmentActivityDomain(1L, null), List.of()))
                .isEqualByComparingTo("30.00");
        assertThat(transformer.calculateSoldProductPrice(
                        appointmentDate, new KokuCustomerAppointmentSoldProductDomain(2L, null), List.of()))
                .isEqualByComparingTo("40.00");
        assertThat(transformer.calculateActivityPrice(
                        appointmentDate,
                        new KokuCustomerAppointmentActivityDomain(1L, new BigDecimal("-1.00")),
                        List.of()))
                .isZero();
        assertThat(transformer.calculateSoldProductPrice(
                        appointmentDate,
                        new KokuCustomerAppointmentSoldProductDomain(2L, new BigDecimal("-1.00")),
                        List.of()))
                .isZero();

        when(activities.get(3L)).thenReturn(ActivityKafkaDto.builder().build());
        assertThat(transformer.calculateActivityPrice(
                        appointmentDate, new KokuCustomerAppointmentActivityDomain(3L, null), List.of()))
                .isZero();
    }
}
