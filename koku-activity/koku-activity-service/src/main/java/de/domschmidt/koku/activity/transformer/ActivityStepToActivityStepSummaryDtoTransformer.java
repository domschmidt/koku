package de.domschmidt.koku.activity.transformer;

import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.dto.activity.KokuActivityStepSummaryDto;

public class ActivityStepToActivityStepSummaryDtoTransformer {

    public KokuActivityStepSummaryDto transformToDto(final ActivityStep model) {
        return KokuActivityStepSummaryDto.builder()
                .id(model.getId())
                .summary(model.getName())
                .build();
    }
}
