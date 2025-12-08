package de.domschmidt.calendar.dto.response;


import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@FieldNameConstants
@JsonTypeName("holiday")
@SuperBuilder
@Data
public class CalendarHolidaySourceConfigDto extends AbstractCalendarListSourceConfigDto {

}
