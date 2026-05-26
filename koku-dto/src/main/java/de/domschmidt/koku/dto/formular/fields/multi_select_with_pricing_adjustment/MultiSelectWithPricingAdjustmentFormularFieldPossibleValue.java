package de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment;

import de.domschmidt.koku.contracts.dto.KokuColor;
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
    KokuColor color;
    String category;
}
