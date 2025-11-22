package de.domschmidt.koku.activity.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class ActivityKafkaDtoSerdes extends Serdes.WrapperSerde<ActivityKafkaDto> {

    public ActivityKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<ActivityKafkaDto> getDeserializer() {
        final JsonDeserializer<ActivityKafkaDto> activityDtoJsonDeserializer = new JsonDeserializer<>(ActivityKafkaDto.class);
        activityDtoJsonDeserializer.setUseTypeHeaders(false);
        return activityDtoJsonDeserializer;
    }

}
