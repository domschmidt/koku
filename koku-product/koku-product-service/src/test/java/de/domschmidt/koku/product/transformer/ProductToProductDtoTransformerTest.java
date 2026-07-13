package de.domschmidt.koku.product.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.dto.product.KokuProductManufacturerDto;
import de.domschmidt.koku.product.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import jakarta.persistence.EntityManager;
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

    @Test
    void dtoContainsManufacturerLifecycleAndEmptyHistoryState() {
        final ProductManufacturer manufacturer = new ProductManufacturer();
        manufacturer.setId(7L);
        manufacturer.setName("Maker");
        final Product product = new Product();
        product.setId(5L);
        product.setVersion(3L);
        product.setName("Serum");
        product.setManufacturer(manufacturer);
        product.setDeleted(true);
        product.setPriceHistory(null);

        final KokuProductDto dto = transformer.transformToDto(product);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getVersion()).isEqualTo(3L);
        assertThat(dto.getManufacturerId()).isEqualTo(7L);
        assertThat(dto.getManufacturerName()).isEqualTo("Maker");
        assertThat(dto.getPrice()).isNull();
        assertThat(dto.getDeleted()).isTrue();
    }

    @Test
    void updateResolvesManufacturerAndAddsOnlyChangedPrices() throws Exception {
        final EntityManager entityManager = mock(EntityManager.class);
        final ProductManufacturer manufacturer = new ProductManufacturer();
        when(entityManager.getReference(ProductManufacturer.class, 7L)).thenReturn(manufacturer);
        final Product product = new Product();
        product.getPriceHistory().add(new ProductPriceHistoryEntry(product, new BigDecimal("10.00")));
        final ProductToProductDtoTransformer entityTransformer = new ProductToProductDtoTransformer(entityManager);

        entityTransformer.transformToEntity(
                product,
                KokuProductDto.builder()
                        .manufacturerId(7L)
                        .name("Serum")
                        .price(new BigDecimal("10.00"))
                        .deleted(true)
                        .build());
        assertThat(product.getPriceHistory()).hasSize(1);
        entityTransformer.transformToEntity(
                product,
                KokuProductDto.builder()
                        .manufacturerId(7L)
                        .price(new BigDecimal("12.00"))
                        .build());

        assertThat(product.getName()).isEqualTo("Serum");
        assertThat(product.getManufacturer()).isSameAs(manufacturer);
        assertThat(product.getPriceHistory()).hasSize(2);
        assertThat(product.getPriceHistory().getLast().getPrice()).isEqualByComparingTo("12.00");
        assertThat(product.isDeleted()).isTrue();
        verify(entityManager, times(2)).getReference(ProductManufacturer.class, 7L);
    }

    @Test
    void updateRequiresManufacturerId() {
        assertThatThrownBy(() -> transformer.transformToEntity(
                        new Product(), KokuProductDto.builder().build()))
                .isInstanceOf(ManufacturerIdNotFoundException.class);
    }

    @Test
    void manufacturerTransformerRoundTripsAndPreservesAbsentFields() {
        final ProductManufacturer manufacturer = new ProductManufacturer();
        manufacturer.setId(9L);
        manufacturer.setVersion(2L);
        manufacturer.setName("Existing");
        final ProductManufacturerToProductManufacturerDtoTransformer manufacturerTransformer =
                new ProductManufacturerToProductManufacturerDtoTransformer();

        final KokuProductManufacturerDto dto = manufacturerTransformer.transformToDto(manufacturer);
        assertThat(dto.getId()).isEqualTo(9L);
        assertThat(dto.getVersion()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Existing");

        manufacturerTransformer.transformToEntity(
                manufacturer, KokuProductManufacturerDto.builder().build());
        assertThat(manufacturer.getName()).isEqualTo("Existing");
        manufacturerTransformer.transformToEntity(
                manufacturer,
                KokuProductManufacturerDto.builder()
                        .name("Updated")
                        .deleted(true)
                        .build());
        assertThat(manufacturer.getName()).isEqualTo("Updated");
        assertThat(manufacturer.isDeleted()).isTrue();
    }
}
