package de.domschmidt.koku.promotion.kafka.promotion.service;

import de.domschmidt.koku.promotion.persistence.PromotionRepository;
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
public class PromotionKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final PromotionRepository promotionRepository;
    final PromotionKafkaService promotionKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND PROMOTION ######");
        this.promotionRepository.findAll().forEach(product -> {
            try {
                this.promotionKafkaService.sendPromotion(product);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending promotion", e);
            }
        });
    }
}
