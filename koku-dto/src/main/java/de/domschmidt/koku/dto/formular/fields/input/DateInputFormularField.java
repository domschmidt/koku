package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("date-input")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DateInputFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;
    LocalDate defaultValue;
}
