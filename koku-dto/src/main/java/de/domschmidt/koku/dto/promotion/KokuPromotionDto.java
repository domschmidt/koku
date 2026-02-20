package de.domschmidt.koku.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class KokuPromotionDto {

    Long id;
    Boolean deleted;
    Long version;

    String name;

    String shortSummary;
    String longSummary;

    BigDecimal activityAbsoluteItemSavings;
    BigDecimal activityAbsoluteSavings;
    BigDecimal activityRelativeItemSavings;
    BigDecimal activityRelativeSavings;
    BigDecimal productAbsoluteItemSavings;
    BigDecimal productAbsoluteSavings;
    BigDecimal productRelativeItemSavings;
    BigDecimal productRelativeSavings;

    LocalDateTime updated;
    LocalDateTime recorded;
}
