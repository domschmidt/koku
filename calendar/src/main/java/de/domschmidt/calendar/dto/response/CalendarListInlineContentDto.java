package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("list")
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarListInlineContentDto extends AbstractCalendarInlineContentDto {

    String listUrl;
    String sourceUrl;
    Integer maxWidthInPx;
    String title;
}
