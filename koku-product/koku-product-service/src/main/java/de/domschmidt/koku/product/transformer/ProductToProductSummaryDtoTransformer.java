package de.domschmidt.koku.product.transformer;

import de.domschmidt.koku.dto.product.KokuProductSummaryDto;
import de.domschmidt.koku.product.persistence.Product;

public class ProductToProductSummaryDtoTransformer {

    public KokuProductSummaryDto transformToDto(final Product model) {
        return KokuProductSummaryDto.builder()
                .id(model.getId())
                .summary(model.getName())
                .build();
    }
}
