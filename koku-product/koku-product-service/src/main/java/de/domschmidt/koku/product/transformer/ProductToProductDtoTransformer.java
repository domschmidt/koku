package de.domschmidt.koku.product.transformer;

import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.product.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ProductToProductDtoTransformer {
    private static final Locale CURRENCY_LOCALE = Locale.GERMANY;

    private final EntityManager entityManager;

    public ProductToProductDtoTransformer(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public KokuProductDto transformToDto(final Product model) {
        final ProductManufacturer manufacturer = model.getManufacturer();
        final BigDecimal price = currentPrice(model);
        return KokuProductDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .manufacturerId(manufacturer != null ? manufacturer.getId() : null)
                .manufacturerName(manufacturer != null ? manufacturer.getName() : null)
                .price(price)
                .formattedPrice(formatPrice(price))
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    private static BigDecimal currentPrice(final Product model) {
        final List<ProductPriceHistoryEntry> priceHistory = model.getPriceHistory();
        return priceHistory != null && !priceHistory.isEmpty()
                ? priceHistory.getLast().getPrice()
                : null;
    }

    private static String formatPrice(final BigDecimal price) {
        return price != null ? NumberFormat.getCurrencyInstance(CURRENCY_LOCALE).format(price) : null;
    }

    public Product transformToEntity(final Product model, final KokuProductDto updatedDto)
            throws ManufacturerIdNotFoundException {

        if (updatedDto.getManufacturerId() == null) {
            throw new ManufacturerIdNotFoundException(updatedDto.getManufacturerId());
        }
        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        model.setManufacturer(
                this.entityManager.getReference(ProductManufacturer.class, updatedDto.getManufacturerId()));
        if (updatedDto.getPrice() != null
                && (model.getPriceHistory().isEmpty()
                        || !updatedDto
                                .getPrice()
                                .equals(model.getPriceHistory().getLast().getPrice()))) {
            model.getPriceHistory().add(new ProductPriceHistoryEntry(model, updatedDto.getPrice()));
        }
        if (updatedDto.getDeleted() != null) {
            model.setDeleted(updatedDto.getDeleted());
        }

        return model;
    }
}
