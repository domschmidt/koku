package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("replace-via-payload")
@EqualsAndHashCode(callSuper = true)
public class CalendarReplaceItemViaPayloadGlobalEventListenerDto extends AbstractCalendarGlobalEventListenerDto {

    String sourceId;
    String deletedPath;
    Object deletedExpression;
}
