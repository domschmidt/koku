package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarFormularInlineContentAfterSavePropagateGlobalEventDto
        extends AbstractCalendarItemInlineFormularContentSaveEventDto {

    String eventName;
}
