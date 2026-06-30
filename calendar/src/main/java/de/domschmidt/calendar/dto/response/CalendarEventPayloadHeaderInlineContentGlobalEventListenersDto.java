package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarEventPayloadHeaderInlineContentGlobalEventListenersDto
        extends AbstractCalendarHeaderInlineContentGlobalEventListenersDto {

    String idPath;
    String titleValuePath;
}
