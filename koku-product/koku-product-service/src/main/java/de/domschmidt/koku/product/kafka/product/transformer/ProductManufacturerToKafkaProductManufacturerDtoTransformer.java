package de.domschmidt.koku.product.kafka.product.transformer;

import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.product.persistence.ProductManufacturer;

public class ProductManufacturerToKafkaProductManufacturerDtoTransformer {

    public ProductManufacturerKafkaDto transformToDto(final ProductManufacturer model) {
        return ProductManufacturerKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .name(model.getName())
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
