package de.domschmidt.koku.user.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class UserKafkaDtoSerdes extends Serdes.WrapperSerde<UserKafkaDto> {

    public UserKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<UserKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<UserKafkaDto> customerDtoJsonDeserializer =
                new JacksonJsonDeserializer<>(UserKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }
}
