package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractCalendarActionDto {

    String id;
    String title;
    String text;
    String icon;
    String imgBase64;
    CalendarActionColorEnumDto color;
    Boolean loading;
}
