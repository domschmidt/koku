package de.domschmidt.koku.activity.kafka.activity.service;

import de.domschmidt.koku.activity.kafka.activity.transformer.ActivityToKafkaActivityDtoTransformer;
import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.persistence.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ActivityKafkaService {

    private final KafkaTemplate<Long, ActivityKafkaDto> productKafkaTemplate;

    @Autowired
    public ActivityKafkaService(
            final KafkaTemplate<Long, ActivityKafkaDto> activityKafkaTemplate
    ) {
        this.productKafkaTemplate = activityKafkaTemplate;
    }

    public SendResult<Long, ActivityKafkaDto> sendActivity(
            final Activity activity
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return this.productKafkaTemplate.send(
                ActivityKafkaDto.TOPIC,
                activity.getId(),
                new ActivityToKafkaActivityDtoTransformer().transformToDto(activity)
        ).get(
                10, TimeUnit.SECONDS
        );
    }
}
