package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.activity.ActivityStepDto;
import de.domschmidt.koku.persistence.model.ActivityStep;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityStepToActivityStepDtoTransformer implements ITransformer<ActivityStep, ActivityStepDto> {

    public List<ActivityStepDto> transformToDtoList(final List<ActivityStep> modelList) {
        final List<ActivityStepDto> result = new ArrayList<>();
        for (final ActivityStep activity : modelList) {
            result.add(transformToDto(activity));
        }
        return result;
    }

    public ActivityStepDto transformToDto(ActivityStep model) {
        return ActivityStepDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .build();
    }

    public ActivityStep transformToEntity(final ActivityStepDto dtoModel) {
        return ActivityStep.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .build();
    }

}
