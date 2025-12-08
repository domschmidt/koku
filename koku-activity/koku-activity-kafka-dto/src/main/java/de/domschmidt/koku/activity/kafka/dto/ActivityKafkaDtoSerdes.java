package de.domschmidt.koku.activity.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class ActivityKafkaDtoSerdes extends Serdes.WrapperSerde<ActivityKafkaDto> {

    public ActivityKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<ActivityKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<ActivityKafkaDto> activityDtoJsonDeserializer = new JacksonJsonDeserializer<>(ActivityKafkaDto.class);
        activityDtoJsonDeserializer.setUseTypeHeaders(false);
        return activityDtoJsonDeserializer;
    }

}
