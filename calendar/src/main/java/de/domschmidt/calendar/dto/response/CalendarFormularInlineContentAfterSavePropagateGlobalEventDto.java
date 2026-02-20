package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
public class CalendarFormularInlineContentAfterSavePropagateGlobalEventDto
        extends AbstractCalendarItemInlineFormularContentSaveEventDto {

    String eventName;
}
