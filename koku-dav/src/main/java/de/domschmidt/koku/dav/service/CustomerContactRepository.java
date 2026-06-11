package de.domschmidt.koku.dav.service;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.dav.kafka.customers.service.CustomerKTableProcessor;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerContactRepository {

    private final CustomerKTableProcessor customerKTableProcessor;

    public CustomerContactRepository(final CustomerKTableProcessor customerKTableProcessor) {
        this.customerKTableProcessor = customerKTableProcessor;
    }

    public List<CustomerKafkaDto> findActiveContacts() {
        return findAllContacts().stream()
                .filter(customer -> !Boolean.TRUE.equals(customer.getDeleted()))
                .toList();
    }

    public List<CustomerKafkaDto> findAllContacts() {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                this.customerKTableProcessor.getCustomers().all(), Spliterator.DISTINCT),
                        false)
                .map(entry -> entry.value)
                .filter(customer -> customer != null)
                .toList();
    }

    public Optional<CustomerKafkaDto> findActiveContact(final long id) {
        return findContact(id).filter(contact -> !Boolean.TRUE.equals(contact.getDeleted()));
    }

    public Optional<CustomerKafkaDto> findContact(final long id) {
        return Optional.ofNullable(this.customerKTableProcessor.getCustomers().get(id));
    }
}
