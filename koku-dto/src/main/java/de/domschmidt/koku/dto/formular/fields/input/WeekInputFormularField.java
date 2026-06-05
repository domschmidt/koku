package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.AbstractFormularContent;
import de.domschmidt.koku.dto.date.YearWeek;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("week-input")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WeekInputFormularField extends AbstractFormularContent {

    String valuePath;
    Boolean required;
    Boolean readonly;
    Boolean disabled;

    String label;
    String placeholder;
    YearWeek defaultValue;
}
