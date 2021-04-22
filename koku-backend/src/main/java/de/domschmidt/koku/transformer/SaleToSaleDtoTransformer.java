package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.SaleDto;
import de.domschmidt.koku.persistence.model.Sale;
import de.domschmidt.koku.transformer.common.ITransformer;

import java.util.ArrayList;
import java.util.List;

public class SaleToSaleDtoTransformer implements ITransformer<Sale, SaleDto> {

    public List<SaleDto> transformToDtoList(final List<Sale> modelList) {
        final List<SaleDto> result = new ArrayList<>();
        for (final Sale sale : modelList) {
            result.add(transformToDto(sale));
        }
        return result;
    }

    public SaleDto transformToDto(final Sale model) {
        return SaleDto.builder()
                .id(model.getId())
                .date(model.getDate())
                .description(model.getDescription())
                .price(model.getPrice())
                .build();
    }

    public Sale transformToEntity(final SaleDto dtoModel) {
        return Sale.builder()
                .id(dtoModel.getId())
                .date(dtoModel.getDate())
                .description(dtoModel.getDescription())
                .price(dtoModel.getPrice())
                .build();
    }
}
