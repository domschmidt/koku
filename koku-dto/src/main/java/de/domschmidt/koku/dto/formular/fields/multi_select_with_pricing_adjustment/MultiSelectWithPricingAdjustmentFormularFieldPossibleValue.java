package de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment;

import de.domschmidt.koku.dto.KokuColorEnum;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

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
