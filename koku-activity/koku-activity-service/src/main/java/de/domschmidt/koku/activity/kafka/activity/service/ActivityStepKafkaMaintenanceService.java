package de.domschmidt.koku.activity.kafka.activity.service;

import de.domschmidt.koku.activity.persistence.ActivityStepRepository;
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
public class ActivityStepKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final ActivityStepRepository activityStepRepository;
    final ActivityStepKafkaService activityStepKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND ACTIVITY STEPS ######");
        this.activityStepRepository.findAll().forEach(activityStep -> {
            try {
                this.activityStepKafkaService.sendActivityStep(activityStep);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending activity step", e);
            }
        });
    }
}
