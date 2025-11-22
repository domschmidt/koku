package de.domschmidt.koku.product.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

public class ProductManufacturerKafkaDtoSerdes extends Serdes.WrapperSerde<ProductManufacturerKafkaDto> {

    public ProductManufacturerKafkaDtoSerdes() {
        super(new JsonSerializer<>(), getDeserializer());
    }

    private static JsonDeserializer<ProductManufacturerKafkaDto> getDeserializer() {
        final JsonDeserializer<ProductManufacturerKafkaDto> productManufacturerDtoJsonDeserializer = new JsonDeserializer<>(ProductManufacturerKafkaDto.class);
        productManufacturerDtoJsonDeserializer.setUseTypeHeaders(false);
        return productManufacturerDtoJsonDeserializer;
    }

}
