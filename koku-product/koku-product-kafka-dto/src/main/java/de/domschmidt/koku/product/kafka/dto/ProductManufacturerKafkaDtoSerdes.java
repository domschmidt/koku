package de.domschmidt.koku.product.kafka.dto;

import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

public class ProductManufacturerKafkaDtoSerdes extends Serdes.WrapperSerde<ProductManufacturerKafkaDto> {

    public ProductManufacturerKafkaDtoSerdes() {
        super(new JacksonJsonSerializer<>(), getDeserializer());
    }

    private static JacksonJsonDeserializer<ProductManufacturerKafkaDto> getDeserializer() {
        final JacksonJsonDeserializer<ProductManufacturerKafkaDto> productManufacturerDtoJsonDeserializer = new JacksonJsonDeserializer<>(ProductManufacturerKafkaDto.class);
        productManufacturerDtoJsonDeserializer.setUseTypeHeaders(false);
        return productManufacturerDtoJsonDeserializer;
    }

}
