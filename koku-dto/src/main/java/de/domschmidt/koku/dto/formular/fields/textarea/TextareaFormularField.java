package de.domschmidt.koku.dto.formular.fields.textarea;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("textarea")
@Getter
public class TextareaFormularField extends AbstractFormField<String> {

    String label;
    String placeholder;
    Integer minLength;
    Integer maxLength;
    String regexp;

    @Builder.Default
    String defaultValue = "";
}
