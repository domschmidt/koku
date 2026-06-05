package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("input")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class InputFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

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
