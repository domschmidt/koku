package de.domschmidt.koku.product.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class ProductKafkaDtoSerdes extends Serdes.WrapperSerde<ProductKafkaDto> {

    public ProductKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<ProductKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<ProductKafkaDto> customerDtoJsonDeserializer = new JacksonJsonDeserializer<>(ProductKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }

}
