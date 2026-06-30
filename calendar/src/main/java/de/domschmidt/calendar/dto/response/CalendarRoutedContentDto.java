package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("routed-inline-content")
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarRoutedContentDto extends AbstractCalendarRoutedContentDto {

    AbstractCalendarInlineContentDto inlineContent;
}
