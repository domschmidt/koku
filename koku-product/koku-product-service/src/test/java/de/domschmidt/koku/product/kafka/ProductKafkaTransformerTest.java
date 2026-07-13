package de.domschmidt.koku.product.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.product.kafka.product.transformer.ProductToKafkaProductDtoTransformer;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductKafkaTransformerTest {
    private final ProductToKafkaProductDtoTransformer transformer = new ProductToKafkaProductDtoTransformer();

    @Test
    void exportsManufacturerAndPriceHistory() {
        final Product product = new Product();
        product.setId(5L);
        product.setName("Serum");
        product.setDeleted(true);
        product.setManufacturer(new ProductManufacturer(7L));
        final ProductPriceHistoryEntry price = new ProductPriceHistoryEntry(product, new BigDecimal("19.90"));
        price.setRecorded(LocalDateTime.of(2026, java.time.Month.JULY, 13, 12, 0));
        product.setPriceHistory(List.of(price));

        final var dto = transformer.transformToDto(product);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getManufacturerId()).isEqualTo(7L);
        assertThat(dto.getPriceHistory()).singleElement().satisfies(entry -> {
            assertThat(entry.getPrice()).isEqualByComparingTo("19.90");
            assertThat(entry.getRecorded()).isEqualTo(LocalDateTime.of(2026, java.time.Month.JULY, 13, 12, 0));
        });
    }

    @Test
    void preservesNullOptionalRelationships() {
        final Product product = new Product();
        product.setPriceHistory(null);

        final var dto = transformer.transformToDto(product);

        assertThat(dto.getManufacturerId()).isNull();
        assertThat(dto.getPriceHistory()).isNull();
    }
}
