package de.domschmidt.koku.dto.list.fields.input;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.koku.dto.KokuColorEnum;
import de.domschmidt.koku.dto.KokuRoundedEnum;
import de.domschmidt.list.dto.response.fields.AbstractListViewFieldDto;
import java.time.YearMonth;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("month-input")
@Getter
public class ListViewMonthInputFieldDto extends AbstractListViewFieldDto<YearMonth> {

    String label;
    KokuRoundedEnum rounded;
    KokuColorEnum backgroundColor;
    YearMonth defaultValue;
}
