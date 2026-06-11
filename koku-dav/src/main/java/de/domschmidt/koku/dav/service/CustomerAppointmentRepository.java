package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerAppointmentKTableProcessor;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerAppointmentRepository {

    private final CustomerAppointmentKTableProcessor customerAppointmentKTableProcessor;

    public CustomerAppointmentRepository(final CustomerAppointmentKTableProcessor customerAppointmentKTableProcessor) {
        this.customerAppointmentKTableProcessor = customerAppointmentKTableProcessor;
    }

    public List<CustomerAppointmentKafkaDto> findActiveAppointments() {
        return findAllAppointments().stream()
                .filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()))
                .toList();
    }

    public List<CustomerAppointmentKafkaDto> findAllAppointments() {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                customerAppointmentKTableProcessor
                                        .getCustomerAppointments()
                                        .all(),
                                Spliterator.DISTINCT),
                        false)
                .map(entry -> entry.value)
                .filter(appointment -> appointment != null)
                .sorted(Comparator.comparing(
                        CustomerAppointmentKafkaDto::getStart, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    public Optional<CustomerAppointmentKafkaDto> findActiveAppointment(final long id) {
        return findAppointment(id).filter(appointment -> !Boolean.TRUE.equals(appointment.getDeleted()));
    }

    public Optional<CustomerAppointmentKafkaDto> findAppointment(final long id) {
        return Optional.ofNullable(
                customerAppointmentKTableProcessor.getCustomerAppointments().get(id));
    }
}
