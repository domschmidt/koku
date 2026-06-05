package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import java.time.LocalTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("time-input")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TimeInputFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;
    LocalTime defaultValue;
}
