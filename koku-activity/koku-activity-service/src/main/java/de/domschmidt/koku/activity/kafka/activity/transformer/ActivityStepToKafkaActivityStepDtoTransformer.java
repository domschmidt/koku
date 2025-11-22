package de.domschmidt.koku.activity.kafka.activity.transformer;


import de.domschmidt.koku.activity.kafka.dto.ActivityStepKafkaDto;
import de.domschmidt.koku.activity.persistence.ActivityStep;

public class ActivityStepToKafkaActivityStepDtoTransformer {

    public ActivityStepKafkaDto transformToDto(final ActivityStep model) {
        return ActivityStepKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .name(model.getName())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
