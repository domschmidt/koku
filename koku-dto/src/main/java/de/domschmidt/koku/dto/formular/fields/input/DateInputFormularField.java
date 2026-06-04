package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import java.time.LocalDate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("date-input")
@Getter
public class DateInputFormularField extends AbstractFormField<LocalDate> {

    String label;
    String placeholder;
    LocalDate defaultValue;
}
