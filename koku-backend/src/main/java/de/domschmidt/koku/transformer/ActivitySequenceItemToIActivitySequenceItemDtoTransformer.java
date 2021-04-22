package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.activity.ActivitySequenceItemDto;
import de.domschmidt.koku.dto.activity.ActivityStepDto;
import de.domschmidt.koku.dto.product.ProductDto;
import de.domschmidt.koku.dto.product.ProductManufacturerDto;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.persistence.model.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivitySequenceItemToIActivitySequenceItemDtoTransformer {

    public List<ActivitySequenceItemDto> combineAndTransformLists(
            final List<ActivityStep> activitySteps,
            final List<Product> products) {
        final List<ActivitySequenceItemDto> result = new ArrayList<>();
        for (final ActivityStep activity : activitySteps) {
            result.add(transformToDto(activity));
        }
        for (final Product product : products) {
            result.add(transformToDto(product));
        }
        return result;
    }

    public ActivitySequenceItemDto transformToDto(ActivityStep model) {
        return ActivityStepDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .build();
    }

    public ActivitySequenceItemDto transformToDto(Product model) {
        return ProductDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .manufacturer(ProductManufacturerDto.builder()
                        .name(model.getManufacturer().getName())
                        .id(model.getManufacturer().getId())
                        .build())
                .build();
    }


}
