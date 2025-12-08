package de.domschmidt.koku.activity.kafka.activity.service;

import de.domschmidt.koku.activity.persistence.ActivityRepository;
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
public class ActivityKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final ActivityRepository activityRepository;
    final ActivityKafkaService activityKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND ACTIVITIES ######");
        this.activityRepository.findAll().forEach(activity -> {
            try {
                this.activityKafkaService.sendActivity(activity);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending activity", e);
            }
        });
    }
}
