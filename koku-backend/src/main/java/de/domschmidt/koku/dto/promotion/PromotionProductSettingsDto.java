package de.domschmidt.koku.dto.promotion;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PromotionProductSettingsDto {

    BigDecimal absoluteSavings;
    BigDecimal relativeSavings;
    BigDecimal absoluteItemSavings;
    BigDecimal relativeItemSavings;

}
