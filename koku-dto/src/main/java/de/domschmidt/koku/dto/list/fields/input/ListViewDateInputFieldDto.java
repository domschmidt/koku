package de.domschmidt.koku.dto.list.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.KokuRoundedEnum;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import java.time.LocalDate;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("date-input")
@Getter
public class ListViewDateInputFieldDto extends AbstractListViewFieldDto<LocalDate> {

    String label;
    KokuRoundedEnum rounded;
    KokuColorEnum backgroundColor;
    LocalDate defaultValue;
}
