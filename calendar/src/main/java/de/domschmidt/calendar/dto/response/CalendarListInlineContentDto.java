package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("list")
@Data
public class CalendarListInlineContentDto extends AbstractCalendarInlineContentDto {

    String listUrl;
    String sourceUrl;
    Integer maxWidthInPx;
    String title;
}
