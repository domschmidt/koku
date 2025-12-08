package de.domschmidt.koku.product.kafka.product.service;

import de.domschmidt.koku.product.persistence.ProductRepository;
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
public class ProductKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final ProductRepository productRepository;
    final ProductKafkaService productKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND PRODUCTS ######");
        this.productRepository.findAll().forEach(product -> {
            try {
                this.productKafkaService.sendProduct(product);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending product", e);
            }
        });
    }
}
