package de.domschmidt.koku.customer.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class CustomerKafkaDtoSerdes extends Serdes.WrapperSerde<CustomerKafkaDto> {

    public CustomerKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<CustomerKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<CustomerKafkaDto> customerDtoJsonDeserializer =
                new JacksonJsonDeserializer<>(CustomerKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }
}
