package de.domschmidt.koku.product.transformer;

import de.domschmidt.koku.dto.product.KokuProductManufacturerDto;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import org.springframework.stereotype.Component;

@Component
public class ProductManufacturerToProductManufacturerDtoTransformer {

    public KokuProductManufacturerDto transformToDto(final ProductManufacturer model) {
        return KokuProductManufacturerDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .recorded(model.getRecorded())
                .updated(model.getUpdated())
                .build();
    }

    public ProductManufacturer transformToEntity(
            final ProductManufacturer model, final KokuProductManufacturerDto updatedDto) {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }

        return model;
    }
}
