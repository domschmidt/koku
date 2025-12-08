package de.domschmidt.koku.user.kafka.users.service;

import de.domschmidt.koku.user.persistence.UserRepository;
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
public class UserKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final UserRepository userRepository;
    final UserKafkaService userKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND USER ######");
        this.userRepository.findAll().forEach(product -> {
            try {
                this.userKafkaService.sendUser(product);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending user", e);
            }
        });
    }
}
