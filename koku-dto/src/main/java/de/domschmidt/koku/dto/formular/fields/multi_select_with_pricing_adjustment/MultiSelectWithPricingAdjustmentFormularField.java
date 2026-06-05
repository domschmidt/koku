package de.domschmidt.koku.dto.formular.fields.multi_select_with_pricing_adjustment;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("multi-select-with-pricing-adjustment")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiSelectWithPricingAdjustmentFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

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
