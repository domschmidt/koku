package de.domschmidt.koku.user.kafka.users.service;

import de.domschmidt.koku.user.persistence.UserAppointmentRepository;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ConditionalOnBooleanProperty("koku.maintenance")
@RequiredArgsConstructor
public class UserAppointmentKafkaMaintenanceService implements ApplicationListener<ApplicationStartedEvent> {

    final UserAppointmentRepository userAppointmentRepository;
    final UserAppointmentKafkaService userAppointmentKafkaService;

    @Override
    @Transactional
    public void onApplicationEvent(final ApplicationStartedEvent event) {
        log.warn("###### MAINTENANCE MODE ###### SEND USER APPOINTMENTS ######");
        userAppointmentRepository.findAll().forEach(userAppointment -> {
            try {
                userAppointmentKafkaService.sendUserAppointment(userAppointment);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Error sending user appointment", e);
            } catch (ExecutionException | TimeoutException e) {
                log.error("Error sending user appointment", e);
            }
        });
    }
}
