package de.domschmidt.koku.dto.calendar;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.calendar.dto.response.AbstractCalendarActionDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("select-user")
@SuperBuilder
@Data
public class CalendarUserSelectionActionDto extends AbstractCalendarActionDto {}
