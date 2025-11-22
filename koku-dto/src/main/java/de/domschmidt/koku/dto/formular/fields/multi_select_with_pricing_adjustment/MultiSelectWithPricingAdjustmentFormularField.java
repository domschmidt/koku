package de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("multi-select-with-pricing-adjustment")
@Getter
public class MultiSelectWithPricingAdjustmentFormularField extends AbstractFormField<List<MultiSelectWithPricingAdjustmentFormularFieldPossibleValue>> {

    String label;
    String placeholder;
    @Builder.Default
    List<MultiSelectWithPricingAdjustmentFormularFieldPossibleValue> possibleValues = new ArrayList<>();
    @Builder.Default
    List<MultiSelectWithPricingAdjustmentFormularFieldPossibleValue> defaultValue = new ArrayList<>();
    String idPathMapping;
    String pricePathMapping;
    Boolean uniqueValues;

}
