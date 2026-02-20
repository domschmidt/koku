package de.domschmidt.calendar.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CalendarInlineDockContentItemDto {

    String id;
    String title;
    String route;
    String icon;
    AbstractCalendarInlineContentDto content;
}
