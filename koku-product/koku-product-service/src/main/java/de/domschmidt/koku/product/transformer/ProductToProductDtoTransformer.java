package de.domschmidt.koku.product.transformer;

import de.domschmidt.koku.dto.product.KokuProductDto;
import de.domschmidt.koku.product.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.product.persistence.Product;
import de.domschmidt.koku.product.persistence.ProductManufacturer;
import de.domschmidt.koku.product.persistence.ProductPriceHistoryEntry;
import jakarta.persistence.EntityManager;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class ProductToProductDtoTransformer {

    private final EntityManager entityManager;

    public ProductToProductDtoTransformer(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public KokuProductDto transformToDto(final Product model) {
        final ProductManufacturer manufacturer = model.getManufacturer();
        return KokuProductDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .manufacturerId(manufacturer != null ? manufacturer.getId() : null)
                .manufacturerName(model.getManufacturer().getName())
                .price(
                        !model.getPriceHistory().isEmpty()
                                ? model.getPriceHistory().getLast().getPrice()
                                : null)
                .formattedPrice(
                        !model.getPriceHistory().isEmpty()
                                ? NumberFormat.getCurrencyInstance(Locale.GERMANY)
                                        .format(model.getPriceHistory()
                                                .getLast()
                                                .getPrice())
                                : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    public Product transformToEntity(final Product model, final KokuProductDto updatedDto)
            throws ManufacturerIdNotFoundException {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        if (updatedDto.getManufacturerId() != null) {
            model.setManufacturer(
                    this.entityManager.getReference(ProductManufacturer.class, updatedDto.getManufacturerId()));
        } else {
            throw new ManufacturerIdNotFoundException(updatedDto.getManufacturerId());
        }
        if (updatedDto.getPrice() != null
                && (model.getPriceHistory().isEmpty()
                        || !updatedDto
                                .getPrice()
                                .equals(model.getPriceHistory().getLast().getPrice()))) {
            model.getPriceHistory().add(new ProductPriceHistoryEntry(model, updatedDto.getPrice()));
        }

        return model;
    }
}
