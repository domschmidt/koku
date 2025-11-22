package de.domschmidt.koku.customer.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class CustomerKafkaDtoSerdes extends Serdes.WrapperSerde<CustomerKafkaDto> {

    public CustomerKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<CustomerKafkaDto> getDeserializer() {
        final JsonDeserializer<CustomerKafkaDto> customerDtoJsonDeserializer = new JsonDeserializer<>(CustomerKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }

}
