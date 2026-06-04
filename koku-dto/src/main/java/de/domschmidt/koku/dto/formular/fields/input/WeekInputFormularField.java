package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import de.domschmidt.koku.dto.date.YearWeek;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("week-input")
@Getter
public class WeekInputFormularField extends AbstractFormField<YearWeek> {

    String label;
    String placeholder;
    YearWeek defaultValue;
}
