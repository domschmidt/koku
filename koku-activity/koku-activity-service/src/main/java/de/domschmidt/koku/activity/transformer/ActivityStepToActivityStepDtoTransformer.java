package de.domschmidt.koku.activity.transformer;

import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.dto.activity.KokuActivityStepDto;
import org.springframework.stereotype.Component;

@Component
public class ActivityStepToActivityStepDtoTransformer {

    public KokuActivityStepDto transformToDto(final ActivityStep model) {
        return KokuActivityStepDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .recorded(model.getRecorded())
                .updated(model.getUpdated())
                .build();
    }

    public ActivityStep transformToEntity(
            final ActivityStep model,
            final KokuActivityStepDto updatedDto
    ) {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }

        return model;
    }
}
