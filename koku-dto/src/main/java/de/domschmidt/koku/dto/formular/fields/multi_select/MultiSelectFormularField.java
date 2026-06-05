package de.domschmidt.koku.dto.formular.fields.multi_select;

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
@JsonTypeName("multi-select")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MultiSelectFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;

    @Builder.Default
    List<MultiSelectFormularFieldPossibleValue> possibleValues = new ArrayList<>();

    @Builder.Default
    List<MultiSelectFormularFieldPossibleValue> defaultValue = new ArrayList<>();

    String idPathMapping;
    Boolean uniqueValues;
}
