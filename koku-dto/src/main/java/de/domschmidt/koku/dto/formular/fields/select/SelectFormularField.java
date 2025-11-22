package de.domschmidt.koku.dto.formular.fields.select;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("select")
@Getter
@FieldNameConstants
public class SelectFormularField extends AbstractFormField<String> {

    String label;
    String placeholder;
    @Builder.Default
    String defaultValue = "";
    List<SelectFormularFieldPossibleValue> possibleValues;

}
