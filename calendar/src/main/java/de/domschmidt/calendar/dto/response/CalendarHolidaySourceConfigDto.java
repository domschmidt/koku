package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@JsonTypeName("holiday")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarHolidaySourceConfigDto extends AbstractCalendarListSourceConfigDto {}
