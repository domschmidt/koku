package de.domschmidt.koku.dto.activity;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuActivityStepSummaryDto {
    Long id;

    String summary;

}
