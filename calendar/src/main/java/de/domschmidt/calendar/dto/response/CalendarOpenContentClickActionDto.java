package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeName("open-content")
@SuperBuilder
@Data
public class CalendarOpenContentClickActionDto extends AbstractCalendarClickActionDto {

    AbstractCalendarInlineContentDto content;
    List<CalendarFormularFieldOverrideDto> fieldOverrides;
    List<CalendarFormularSourceOverrideDto> sourceOverrides;

}
