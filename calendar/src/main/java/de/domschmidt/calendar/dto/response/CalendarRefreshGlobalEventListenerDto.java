package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("refresh")
public class CalendarRefreshGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {}
