package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-routed-content")
@SuperBuilder
@Data
public class CalendarOpenRoutedContentActionDto extends AbstractCalendarActionDto {

    String route;
}
