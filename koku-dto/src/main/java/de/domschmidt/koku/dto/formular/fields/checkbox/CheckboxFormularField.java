package de.domschmidt.koku.dto.formular.fields.checkbox;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("checkbox")
@Getter
public class CheckboxFormularField extends AbstractFormField<Boolean> {

    String label;
    String placeholder;
    Integer minLength;
    Integer maxLength;
    String regexp;

    @Builder.Default
    Boolean defaultValue = Boolean.FALSE;
}
