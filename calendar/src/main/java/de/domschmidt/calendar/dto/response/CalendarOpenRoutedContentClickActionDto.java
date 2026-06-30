package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-routed-content")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarOpenRoutedContentClickActionDto extends AbstractCalendarClickActionDto {

    String route;
}
