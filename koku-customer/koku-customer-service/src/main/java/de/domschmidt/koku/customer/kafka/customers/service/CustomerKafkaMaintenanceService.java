package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.persistence.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@ConditionalOnBooleanProperty("koku.maintenance")
@RequiredArgsConstructor
public class CustomerKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final CustomerRepository customerRepository;
    final CustomerKafkaService customerKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND CUSTOMERS ######");
        this.customerRepository.findAll().forEach(customer -> {
            try {
                this.customerKafkaService.sendCustomer(customer);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending customer ", e);
            }
        });
    }
}
