package de.domschmidt.koku.dto.formular.fields.select;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("select")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@FieldNameConstants
public class SelectFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;

    @Builder.Default
    String defaultValue = "";

    List<SelectFormularFieldPossibleValue> possibleValues;
}
