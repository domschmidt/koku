package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import java.time.YearMonth;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("month-input")
@Getter
public class MonthInputFormularField extends AbstractFormField<YearMonth> {

    String label;
    String placeholder;
    YearMonth defaultValue;
}
