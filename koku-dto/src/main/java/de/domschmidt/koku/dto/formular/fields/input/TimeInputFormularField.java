package de.domschmidt.koku.dto.formular.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.formular.dto.content.fields.AbstractFormField;
import java.time.LocalTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("time-input")
@Getter
public class TimeInputFormularField extends AbstractFormField<LocalTime> {

    String label;
    String placeholder;
    LocalTime defaultValue;
}
