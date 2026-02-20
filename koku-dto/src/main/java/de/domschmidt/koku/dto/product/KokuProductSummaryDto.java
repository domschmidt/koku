package de.domschmidt.koku.dto.product;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuProductSummaryDto {

    Long id;
    String summary;
}
