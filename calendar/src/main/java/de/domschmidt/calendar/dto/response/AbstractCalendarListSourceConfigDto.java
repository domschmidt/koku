package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractCalendarListSourceConfigDto {

    String id;
    String name;
    String activateToggleIcon;
    CalendarListSourceColorEnumDto sourceItemColor;
    AbstractCalendarItemClickAction clickAction;
    AbstractCalendarItemMoveAction dropAction;
    AbstractCalendarItemResizeAction resizeAction;

}
