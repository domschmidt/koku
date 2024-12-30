package de.domschmidt.koku.carddav.kafka.customers.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class CustomerDtoSerdes extends Serdes.WrapperSerde<CustomerDto> {

    public CustomerDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<CustomerDto> getDeserializer() {
        final JsonDeserializer<CustomerDto> customerDtoJsonDeserializer = new JsonDeserializer<>(CustomerDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }

}
