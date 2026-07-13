package de.domschmidt.koku.customer.kafka.customers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKafkaMaintenanceService;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKafkaService;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaMaintenanceService;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.customer.kafka.customers.transformer.CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer;
import de.domschmidt.koku.customer.kafka.customers.transformer.CustomerToKafkaCustomerDtoTransformer;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentActivity;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentPromotion;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentRepository;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.customer.persistence.CustomerRepository;
import de.domschmidt.koku.customer.service.PhoneNumberNormalizer;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class CustomerKafkaContractTest {

    @Test
    void customerExportContainsIdentityAndProfile() {
        final Customer customer = new Customer();
        customer.setId(5L);
        customer.setFirstname("Ada");
        customer.setLastname("Lovelace");
        customer.setAsthma(true);
        customer.setDeleted(true);

        final var dto = new CustomerToKafkaCustomerDtoTransformer().transformToDto(customer);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getFullname()).isEqualTo("Ada Lovelace");
        assertThat(dto.isAsthma()).isTrue();
        assertThat(dto.getDeleted()).isTrue();
    }

    @Test
    void appointmentExportContainsNestedCommercialContent() {
        final Customer customer = new Customer();
        customer.setId(7L);
        final CustomerAppointment appointment = appointment(customer);
        appointment.setActivities(List.of(new CustomerAppointmentActivity(appointment, 1L, BigDecimal.TEN, 0)));
        appointment.setPromotions(List.of(new CustomerAppointmentPromotion(appointment, 2L, 0)));
        appointment.setSoldProducts(List.of(new CustomerAppointmentSoldProduct(appointment, 3L, BigDecimal.ONE, 0)));

        final var dto = new CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer().transformToDto(appointment);

        assertThat(dto.getCustomerId()).isEqualTo(7L);
        assertThat(dto.getActivities()).singleElement().extracting("activityId").isEqualTo(1L);
        assertThat(dto.getPromotions())
                .singleElement()
                .extracting("promotionId")
                .isEqualTo(2L);
        assertThat(dto.getSoldProducts())
                .singleElement()
                .extracting("productId")
                .isEqualTo(3L);
    }

    @Test
    void appointmentExportUsesEmptyCollectionsForMissingNestedContent() {
        final CustomerAppointment appointment = appointment(new Customer());
        appointment.setActivities(null);
        appointment.setPromotions(null);
        appointment.setSoldProducts(null);

        final var dto = new CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer().transformToDto(appointment);

        assertThat(dto.getActivities()).isEmpty();
        assertThat(dto.getPromotions()).isEmpty();
        assertThat(dto.getSoldProducts()).isEmpty();
    }

    @Test
    void maintenanceRefreshesSnapshotsAndPublishesEveryAppointment() throws Exception {
        final CustomerAppointmentRepository repository = mock(CustomerAppointmentRepository.class);
        final CustomerAppointmentKafkaService kafkaService = mock(CustomerAppointmentKafkaService.class);
        final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer =
                mock(CustomerAppointmentToCustomerAppointmentDtoTransformer.class);
        final CustomerAppointment appointment = appointment(new Customer());
        appointment.setActivities(List.of(new CustomerAppointmentActivity(appointment, 1L, BigDecimal.TEN, 0)));
        appointment.setPromotions(List.of());
        appointment.setSoldProducts(List.of(new CustomerAppointmentSoldProduct(appointment, 2L, BigDecimal.ONE, 0)));
        when(repository.findAll()).thenReturn(List.of(appointment));
        when(transformer.calculateCustomerAppointmentSoldProductPriceSum(any(), anyList(), anyList()))
                .thenReturn(new BigDecimal("12.00"));
        when(transformer.calculateCustomerAppointmentActivityPriceSum(any(), anyList(), anyList()))
                .thenReturn(new BigDecimal("34.00"));
        when(transformer.calculateCustomerAppointmentSoldProductSummary(anyList()))
                .thenReturn("Product");
        when(transformer.calculateCustomerAppointmentActivitySummary(anyList())).thenReturn("Activity");
        when(transformer.calculateCustomerAppointmentEnd(any(), anyList()))
                .thenReturn(appointment.getStart().plusHours(1));
        when(transformer.calculateSoldProductPrice(any(), any(), anyList())).thenReturn(BigDecimal.ONE);
        when(transformer.calculateActivityPrice(any(), any(), anyList())).thenReturn(BigDecimal.TEN);

        new CustomerAppointmentKafkaMaintenanceService(repository, kafkaService, transformer).runMaintenance();

        assertThat(appointment.getSoldProductsRevenueSnapshot()).isEqualByComparingTo("12.00");
        assertThat(appointment.getActivitiesRevenueSnapshot()).isEqualByComparingTo("34.00");
        assertThat(appointment.getCalculatedEndSnapshot())
                .isEqualTo(appointment.getStart().plusHours(1));
        verify(kafkaService).sendCustomerAppointment(appointment);
    }

    @Test
    void customerMaintenanceNormalizesPhoneNumbersBeforePublishing() throws Exception {
        final CustomerRepository repository = mock(CustomerRepository.class);
        final CustomerKafkaService kafkaService = mock(CustomerKafkaService.class);
        final PhoneNumberNormalizer normalizer = mock(PhoneNumberNormalizer.class);
        final Customer customer = new Customer();
        customer.setPrivateTelephoneNo("private");
        customer.setBusinessTelephoneNo("business");
        customer.setMobileTelephoneNo("mobile");
        when(repository.findAll()).thenReturn(List.of(customer));
        when(normalizer.normalize("private")).thenReturn("private-normalized");
        when(normalizer.normalize("business")).thenReturn("business-normalized");
        when(normalizer.normalize("mobile")).thenReturn("mobile-normalized");

        new CustomerKafkaMaintenanceService(repository, kafkaService, normalizer).onApplicationEvent(null);

        assertThat(customer.getPrivateTelephoneNo()).isEqualTo("private-normalized");
        assertThat(customer.getBusinessTelephoneNo()).isEqualTo("business-normalized");
        assertThat(customer.getMobileTelephoneNo()).isEqualTo("mobile-normalized");
        verify(kafkaService).sendCustomer(customer);
    }

    @Test
    void customerMaintenanceRestoresInterruptStatus() throws Exception {
        final CustomerRepository repository = mock(CustomerRepository.class);
        final CustomerKafkaService kafkaService = mock(CustomerKafkaService.class);
        final PhoneNumberNormalizer normalizer = mock(PhoneNumberNormalizer.class);
        final Customer customer = new Customer();
        when(repository.findAll()).thenReturn(List.of(customer));
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendCustomer(customer);

        new CustomerKafkaMaintenanceService(repository, kafkaService, normalizer).onApplicationEvent(null);

        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    void customerMaintenanceContinuesAfterKafkaExecutionFailure() throws Exception {
        final CustomerRepository repository = mock(CustomerRepository.class);
        final CustomerKafkaService kafkaService = mock(CustomerKafkaService.class);
        final PhoneNumberNormalizer normalizer = mock(PhoneNumberNormalizer.class);
        final Customer customer = new Customer();
        when(repository.findAll()).thenReturn(List.of(customer));
        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(kafkaService)
                .sendCustomer(customer);

        new CustomerKafkaMaintenanceService(repository, kafkaService, normalizer).onApplicationEvent(null);

        verify(kafkaService).sendCustomer(customer);
    }

    private static CustomerAppointment appointment(Customer customer) {
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setId(9L);
        appointment.setCustomer(customer);
        appointment.setUserId("u-1");
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 13, 9, 0));
        return appointment;
    }
}
