package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.product.ProductDto;
import de.domschmidt.koku.dto.product.ProductManufacturerDto;
import de.domschmidt.koku.persistence.model.Product;
import de.domschmidt.koku.persistence.model.ProductManufacturer;
import de.domschmidt.koku.persistence.model.ProductPriceHistoryEntry;
import de.domschmidt.koku.service.searchoptions.ProductSearchOptions;
import de.domschmidt.koku.transformer.common.ITransformer;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductToProductDtoTransformer implements ITransformer<Product, ProductDto> {

    @Override
    public List<ProductDto> transformToDtoList(final List<Product> modelList) {
        final List<ProductDto> result = new ArrayList<>();
        for (final Product product : modelList) {
            result.add(transformToDto(product, false));
        }
        return result;
    }

    public List<ProductDto> transformToDtoList(final List<Product> modelList, final ProductSearchOptions searchOptions) {
        final List<ProductDto> result = new ArrayList<>();
        for (final Product product : modelList) {
            result.add(transformToDto(product, false, searchOptions));
        }
        return result;
    }

    @Override
    public ProductDto transformToDto(final Product model) {
        return transformToDto(model, true);
    }

    public ProductDto transformToDto(final Product model, final boolean detailed) {
        final BigDecimal currentPrice;
        List<ProductPriceHistoryEntry> priceHistory = model.getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        if (!priceHistory.isEmpty()) {
            currentPrice = priceHistory.get(priceHistory.size() - 1).getPrice();
        } else {
            currentPrice = BigDecimal.ZERO;
        }
        return transformToDtoUsingPrice(model, currentPrice, detailed);
    }

    private ProductDto transformToDtoUsingPrice(Product model, BigDecimal currentPrice, boolean detailed) {
        List<ProductPriceHistoryEntry> priceHistory = model.getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        return ProductDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .currentPrice(currentPrice)
                .manufacturer(ProductManufacturerDto.builder()
                        .id(model.getManufacturer().getId())
                        .name(model.getManufacturer().getName())
                        .build())
                .priceHistory(detailed ? new ProductPriceHistoryEntryToPriceHistoryDtoTransformer().transformToDtoList(priceHistory) : null)
                .build();
    }

    public ProductDto transformToDto(final Product model, final boolean detailed, final ProductSearchOptions searchOptions) {
        if (searchOptions.getPriceDate() != null && searchOptions.getPriceTime() != null) {
            final BigDecimal currentPrice = ProductPriceUtils.getPriceFromHistory(model, searchOptions.getPriceDate().atTime(searchOptions.getPriceTime()));
            return transformToDtoUsingPrice(model, currentPrice, detailed);
        } else {
            return transformToDto(model, detailed);
        }
    }

    @Override
    public Product transformToEntity(final ProductDto productDto) {
        return Product.builder()
                .id(productDto.getId())
                .manufacturer(ProductManufacturer.builder()
                        .id(productDto.getManufacturer().getId())
                        .name(productDto.getManufacturer().getName() != null ? productDto.getManufacturer().getName() : null)
                        .build())
                .description(productDto.getDescription())
                .build();
    }

    public ProductPriceHistoryEntry createNewPriceEntry(final ProductDto updatedDto, final Product mergedProduct) {
        return ProductPriceHistoryEntry.builder()
                .price(updatedDto.getCurrentPrice())
                .product(mergedProduct)
                .build();
    }
}
