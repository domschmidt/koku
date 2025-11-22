package de.domschmidt.koku.activity.kafka.activity.service;

import de.domschmidt.koku.activity.kafka.activity.transformer.ActivityStepToKafkaActivityStepDtoTransformer;
import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.activity.persistence.ActivityStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class ActivityStepKafkaService {

    private final KafkaTemplate<Long, ActivityStepKafkaDto> activityStepKafkaTemplate;

    @Autowired
    public ActivityStepKafkaService(
            final KafkaTemplate<Long, ActivityStepKafkaDto> activityStepKafkaTemplate
    ) {
        this.activityStepKafkaTemplate = activityStepKafkaTemplate;
    }

    public SendResult<Long, ActivityStepKafkaDto> sendActivityStep(
            final ActivityStep activityStep
    ) throws ExecutionException, InterruptedException, TimeoutException {
        return this.activityStepKafkaTemplate.send(
                ActivityStepKafkaDto.TOPIC,
                activityStep.getId(),
                new ActivityStepToKafkaActivityStepDtoTransformer().transformToDto(activityStep)
        ).get(
                10, TimeUnit.SECONDS
        );
    }
}
