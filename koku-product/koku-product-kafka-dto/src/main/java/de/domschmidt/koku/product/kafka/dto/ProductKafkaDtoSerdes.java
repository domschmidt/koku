package de.domschmidt.koku.product.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class ProductKafkaDtoSerdes extends Serdes.WrapperSerde<ProductKafkaDto> {

    public ProductKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<ProductKafkaDto> getDeserializer() {
        final JsonDeserializer<ProductKafkaDto> customerDtoJsonDeserializer = new JsonDeserializer<>(ProductKafkaDto.class);
        customerDtoJsonDeserializer.setUseTypeHeaders(false);
        return customerDtoJsonDeserializer;
    }

}
