package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.activity.ActivityCategoryDto;
import de.domschmidt.koku.persistence.model.ActivityCategory;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityCategoryToActivityCategoryDtoTransformer implements ITransformer<ActivityCategory, ActivityCategoryDto> {

    @Override
    public List<ActivityCategoryDto> transformToDtoList(
            final List<ActivityCategory> modelList
    ) {
        final List<ActivityCategoryDto> result = new ArrayList<>();
        for (final ActivityCategory currentCategory : modelList) {
            result.add(transformToDto(currentCategory));
        }
        return result;
    }

    @Override
    public ActivityCategoryDto transformToDto(
            final ActivityCategory model
    ) {
        final ActivityCategoryDto result = new ActivityCategoryDto();

        result.setDescription(model.getDescription());
        result.setId(model.getId());

        return result;
    }

    @Override
    public ActivityCategory transformToEntity(
            final ActivityCategoryDto inputDto
    ) {
        final ActivityCategory result = new ActivityCategory();

        result.setDescription(inputDto.getDescription());
        result.setId(inputDto.getId());

        return result;
    }
}
