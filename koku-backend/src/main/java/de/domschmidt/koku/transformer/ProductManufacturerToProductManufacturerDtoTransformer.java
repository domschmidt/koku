package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.product.ProductManufacturerDto;
import de.domschmidt.koku.persistence.model.ProductManufacturer;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductManufacturerToProductManufacturerDtoTransformer implements ITransformer<ProductManufacturer, ProductManufacturerDto> {

    @Override
    public List<ProductManufacturerDto> transformToDtoList(final List<ProductManufacturer> modelList) {
        final List<ProductManufacturerDto> result = new ArrayList<>();
        for (final ProductManufacturer productManufacturer : modelList) {
            result.add(transformToDto(productManufacturer));
        }
        return result;
    }

    @Override
    public ProductManufacturerDto transformToDto(final ProductManufacturer model) {
        return ProductManufacturerDto.builder()
                .id(model.getId())
                .name(model.getName())
                .build();
    }

    @Override
    public ProductManufacturer transformToEntity(final ProductManufacturerDto productManufacturerDto) {
        return ProductManufacturer.builder()
                .id(productManufacturerDto.getId())
                .name(productManufacturerDto.getName())
                .build();
    }
}
