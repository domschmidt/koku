package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("flash-once")
public class CalendarFlashOnceGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {

    String sourceId;
}
