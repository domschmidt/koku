package de.domschmidt.koku.dto.promotion;


import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder

@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuPromotionSummaryDto {

    Long id;

    String summary;

}
