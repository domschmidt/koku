package de.domschmidt.koku.product.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductToProductDtoTransformerTest {

    private final ProductToProductDtoTransformer transformer = new ProductToProductDtoTransformer(null);

    @Test
    void shouldHandleNullCurrentPrice() {
        final Product product = new Product();
        product.getPriceHistory().add(new ProductPriceHistoryEntry(product, null));

        final KokuProductDto dto = this.transformer.transformToDto(product);

        assertThat(dto.getPrice()).isNull();
        assertThat(dto.getFormattedPrice()).isNull();
    }

    @Test
    void shouldFormatCurrentPrice() {
        final Product product = new Product();
        product.getPriceHistory().add(new ProductPriceHistoryEntry(product, new BigDecimal("12.34")));

        final KokuProductDto dto = this.transformer.transformToDto(product);

        assertThat(dto.getPrice()).isEqualByComparingTo("12.34");
        assertThat(dto.getFormattedPrice()).contains("12,34");
    }
}
