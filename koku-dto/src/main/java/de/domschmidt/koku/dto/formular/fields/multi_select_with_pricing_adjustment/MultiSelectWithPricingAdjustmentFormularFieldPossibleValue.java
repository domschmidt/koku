package de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment;

import de.domschmidt.koku.dto.KokuColorEnum;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class MultiSelectWithPricingAdjustmentFormularFieldPossibleValue {

    String id;
    String text;
    Boolean disabled;
    BigDecimal defaultPrice;
    KokuColorEnum color;
    String category;

}
