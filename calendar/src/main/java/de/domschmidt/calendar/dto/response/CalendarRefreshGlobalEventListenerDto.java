package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("refresh")
@EqualsAndHashCode(callSuper = true)
public class CalendarRefreshGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {}
