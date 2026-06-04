package de.domschmidt.koku.dto.list.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.KokuRoundedEnum;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import java.time.LocalTime;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("time-input")
@Getter
public class ListViewTimeInputFieldDto extends AbstractListViewFieldDto<LocalTime> {

    String label;
    KokuRoundedEnum rounded;
    KokuColorEnum backgroundColor;
    LocalTime defaultValue;
}
