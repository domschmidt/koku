package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.PriceHistoryDto;
import de.domschmidt.koku.persistence.model.ProductPriceHistoryEntry;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductPriceHistoryEntryToPriceHistoryDtoTransformer implements ITransformer<ProductPriceHistoryEntry, PriceHistoryDto> {

    @Override
    public List<PriceHistoryDto> transformToDtoList(final List<ProductPriceHistoryEntry> modelList) {
        final List<PriceHistoryDto> result = new ArrayList<>();
        for (final ProductPriceHistoryEntry productPriceHistoryEntry : modelList) {
            result.add(transformToDto(productPriceHistoryEntry));
        }
        return result;
    }

    @Override
    public PriceHistoryDto transformToDto(final ProductPriceHistoryEntry model) {
        return PriceHistoryDto.builder()
                .price(model.getPrice())
                .recorded(model.getRecorded())
                .build();
    }

    @Override
    public ProductPriceHistoryEntry transformToEntity(final PriceHistoryDto dtoModel) {
        return ProductPriceHistoryEntry.builder()
                .price(dtoModel.getPrice())
                .build();
    }
}
