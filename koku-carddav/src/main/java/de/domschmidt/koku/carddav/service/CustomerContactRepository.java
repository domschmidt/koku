package de.domschmidt.koku.carddav.service;

import de.domschmidt.koku.carddav.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
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
        return findActiveContacts().stream()
                .filter(contact -> id == contact.getId())
                .findFirst();
    }
}
