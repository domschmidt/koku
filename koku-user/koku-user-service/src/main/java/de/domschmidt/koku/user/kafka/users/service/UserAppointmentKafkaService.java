package de.domschmidt.koku.user.kafka.users.service;

import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import de.domschmidt.koku.user.kafka.users.transformer.UserAppointmentToKafkaUserAppointmentDtoTransformer;
import de.domschmidt.koku.user.persistence.UserAppointment;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class UserAppointmentKafkaService {

    private final KafkaTemplate<Long, UserAppointmentKafkaDto> userAppointmentKafkaTemplate;

    public UserAppointmentKafkaService(
            final KafkaTemplate<Long, UserAppointmentKafkaDto> userAppointmentKafkaTemplate) {
        this.userAppointmentKafkaTemplate = userAppointmentKafkaTemplate;
    }

    public SendResult<Long, UserAppointmentKafkaDto> sendUserAppointment(final UserAppointment userAppointment)
            throws ExecutionException, InterruptedException, TimeoutException {
        return userAppointmentKafkaTemplate
                .send(
                        UserAppointmentKafkaDto.TOPIC,
                        userAppointment.getId(),
                        new UserAppointmentToKafkaUserAppointmentDtoTransformer().transformToDto(userAppointment))
                .get(10, TimeUnit.SECONDS);
    }
}
