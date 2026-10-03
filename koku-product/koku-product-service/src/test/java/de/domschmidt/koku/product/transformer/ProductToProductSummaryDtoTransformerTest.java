package de.domschmidt.koku.product.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.dto.product.KokuProductSummaryDto;
import de.domschmidt.koku.product.persistence.Product;
import org.junit.jupiter.api.Test;

class ProductToProductSummaryDtoTransformerTest {

    private final ProductToProductSummaryDtoTransformer transformer = new ProductToProductSummaryDtoTransformer();

    @Test
    void summaryContainsMilliliters() {
        final Product product = new Product();
        product.setId(5L);
        product.setName("Shampoo");
        product.setMilliliters(250);

        final KokuProductSummaryDto dto = transformer.transformToDto(product);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getSummary()).isEqualTo("Shampoo (250 ml)");
    }

    @Test
    void summaryWithoutMillilitersIsThePlainName() {
        final Product product = new Product();
        product.setName("Shampoo");
        product.setMilliliters(null);

        assertThat(transformer.transformToDto(product).getSummary()).isEqualTo("Shampoo");
    }
}
