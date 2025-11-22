package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("input")
@Getter
public class InputFormularField extends AbstractFormField<String> {

    @Builder.Default
    EnumInputFormularFieldType type = EnumInputFormularFieldType.TEXT;
    String label;
    String placeholder;
    Integer minLength;
    Integer maxLength;
    String regexp;
    @Builder.Default
    String defaultValue = "";

}
