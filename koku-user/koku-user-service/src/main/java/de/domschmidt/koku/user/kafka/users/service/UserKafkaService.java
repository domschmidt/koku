package de.domschmidt.koku.user.kafka.users.service;

import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.koku.user.kafka.users.transformer.UserToKafkaUserDtoTransformer;
import de.domschmidt.koku.user.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UserKafkaService {

    private final KafkaTemplate<String, UserKafkaDto> userKafkaTemplate;

    @Autowired
    public UserKafkaService(
            final KafkaTemplate<String, UserKafkaDto> userKafkaTemplate
    ) {
        this.userKafkaTemplate = userKafkaTemplate;
    }

    public SendResult<String, UserKafkaDto> sendUser(
            final User user
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return this.userKafkaTemplate.send(
                UserKafkaDto.TOPIC,
                user.getId(),
                new UserToKafkaUserDtoTransformer().transformToDto(user)
        ).get(
                10, TimeUnit.SECONDS
        );
    }
}
