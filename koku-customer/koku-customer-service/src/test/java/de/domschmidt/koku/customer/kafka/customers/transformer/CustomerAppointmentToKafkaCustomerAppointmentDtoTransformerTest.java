package de.domschmidt.koku.customer.kafka.customers.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import org.junit.jupiter.api.Test;

class CustomerAppointmentToKafkaCustomerAppointmentDtoTransformerTest {

    private final CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer transformer =
            new CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer();

    @Test
    void mapsCalculatedEndSnapshot() {
        final LocalDateTime calculatedEnd = LocalDateTime.of(2026, Month.JULY, 15, 12, 30);
        final CustomerAppointment appointment = mock(CustomerAppointment.class);
        final Customer customer = mock(Customer.class);
        when(appointment.getCalculatedEndSnapshot()).thenReturn(calculatedEnd);
        when(appointment.getCustomer()).thenReturn(customer);
        when(appointment.getActivities()).thenReturn(List.of());
        when(appointment.getPromotions()).thenReturn(List.of());
        when(appointment.getSoldProducts()).thenReturn(List.of());

        assertThat(transformer.transformToDto(appointment).getEnd()).isEqualTo(calculatedEnd);
    }
}
