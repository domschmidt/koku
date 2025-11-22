package de.domschmidt.koku.activity.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class ActivityStepKafkaDtoSerdes extends Serdes.WrapperSerde<ActivityStepKafkaDto> {

    public ActivityStepKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<ActivityStepKafkaDto> getDeserializer() {
        final JsonDeserializer<ActivityStepKafkaDto> activityStepDtoJsonDeserializer = new JsonDeserializer<>(ActivityStepKafkaDto.class);
        activityStepDtoJsonDeserializer.setUseTypeHeaders(false);
        return activityStepDtoJsonDeserializer;
    }

}
