package de.domschmidt.koku.user.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class UserKafkaDtoSerdes extends Serdes.WrapperSerde<UserKafkaDto> {

    public UserKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<UserKafkaDto> getDeserializer() {
        final JsonDeserializer<UserKafkaDto> customerDtoJsonDeserializer = new JsonDeserializer<>(UserKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }

}
