package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("replace-via-payload")
public class CalendarReplaceItemViaPayloadGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {

    String sourceId;
    String deletedPath;
    Object deletedExpression;
}
