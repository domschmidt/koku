package de.domschmidt.koku.dto.formular.fields.multi_select;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("multi-select")
@Getter
public class MultiSelectFormularField extends AbstractFormField<List<MultiSelectFormularFieldPossibleValue>> {

    String label;
    String placeholder;
    @Builder.Default
    List<MultiSelectFormularFieldPossibleValue> possibleValues = new ArrayList<>();
    @Builder.Default
    List<MultiSelectFormularFieldPossibleValue> defaultValue = new ArrayList<>();
    String idPathMapping;
    Boolean uniqueValues;

}
