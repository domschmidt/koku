package de.domschmidt.koku.dto.list.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.KokuRoundedEnum;
import de.domschmidt.koku.dto.date.YearWeek;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("week-input")
@Getter
public class ListViewWeekInputFieldDto extends AbstractListViewFieldDto<YearWeek> {

    String label;
    KokuRoundedEnum rounded;
    KokuColorEnum backgroundColor;
    YearWeek defaultValue;
}
