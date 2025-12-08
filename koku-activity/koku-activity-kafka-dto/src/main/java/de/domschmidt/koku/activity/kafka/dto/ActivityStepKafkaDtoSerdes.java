package de.domschmidt.koku.activity.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class ActivityStepKafkaDtoSerdes extends Serdes.WrapperSerde<ActivityStepKafkaDto> {

    public ActivityStepKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<ActivityStepKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<ActivityStepKafkaDto> activityStepDtoJsonDeserializer = new JacksonJsonDeserializer<>(ActivityStepKafkaDto.class);
        activityStepDtoJsonDeserializer.setUseTypeHeaders(false);
        return activityStepDtoJsonDeserializer;
    }

}
