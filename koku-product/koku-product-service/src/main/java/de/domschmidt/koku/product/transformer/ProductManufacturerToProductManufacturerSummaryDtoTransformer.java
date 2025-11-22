package de.domschmidt.koku.product.transformer;

import de.domschmidt.koku.dto.product.KokuProductManufacturerSummaryDto;
import de.domschmidt.koku.product.persistence.ProductManufacturer;

public class ProductManufacturerToProductManufacturerSummaryDtoTransformer {

    public KokuProductManufacturerSummaryDto transformToDto(final ProductManufacturer model) {
        return KokuProductManufacturerSummaryDto.builder()
                .id(model.getId())
                .summary(model.getName())
                .build();
    }
}
