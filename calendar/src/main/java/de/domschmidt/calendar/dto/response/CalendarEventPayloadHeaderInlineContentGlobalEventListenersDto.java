package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class CalendarEventPayloadHeaderInlineContentGlobalEventListenersDto
        extends AbstractCalendarHeaderInlineContentGlobalEventListenersDto {

    String idPath;
    String titleValuePath;
}
