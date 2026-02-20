package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("routed-inline-content")
@Data
public class CalendarRoutedContentDto extends AbstractCalendarRoutedContentDto {

    AbstractCalendarInlineContentDto inlineContent;
}
