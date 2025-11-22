package de.domschmidt.koku.activity.transformer;

import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.dto.activity.KokuActivitySummaryDto;

public class ActivityToActivitySummaryDtoTransformer {

    public KokuActivitySummaryDto transformToDto(final Activity model) {
        return KokuActivitySummaryDto.builder()
                .id(model.getId())
                .summary(model.getName())
                .build();
    }

}
