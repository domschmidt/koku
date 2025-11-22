package de.domschmidt.koku.dto.activity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuActivityStepSummaryDto {
    Long id;

    String summary;

}
