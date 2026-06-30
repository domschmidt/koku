package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("close")
@EqualsAndHashCode(callSuper = true)
public class CalendarRoutedContentCloseGlobalEventListenerDto
        extends AbstractCalendarRoutedContentGlobalEventListenerDto {}
