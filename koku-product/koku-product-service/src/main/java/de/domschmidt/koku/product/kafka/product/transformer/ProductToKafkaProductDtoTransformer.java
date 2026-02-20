package de.domschmidt.koku.product.kafka.product.transformer;

import de.domschmidt.koku.product.kafka.dto.ProductKafkaDto;
import de.domschmidt.koku.product.kafka.dto.ProductPriceHistoryKafkaDto;
import de.domschmidt.koku.product.persistence.Product;

public class ProductToKafkaProductDtoTransformer {

    public ProductKafkaDto transformToDto(final Product model) {
        return ProductKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .name(model.getName())
                .priceHistory(
                        model.getPriceHistory() != null
                                ? model.getPriceHistory().stream()
                                        .map(productPriceHistoryEntry -> ProductPriceHistoryKafkaDto.builder()
                                                .price(productPriceHistoryEntry.getPrice())
                                                .recorded(productPriceHistoryEntry.getRecorded())
                                                .build())
                                        .toList()
                                : null)
                .manufacturerId(
                        model.getManufacturer() != null
                                ? model.getManufacturer().getId()
                                : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
