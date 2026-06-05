package de.domschmidt.koku.dto.formular.fields.textarea;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("textarea")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TextareaFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;
    Integer minLength;
    Integer maxLength;
    String regexp;

    @Builder.Default
    String defaultValue = "";
}
