package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-content")
@SuperBuilder
@Data
public class CalendarOpenContentClickActionDto extends AbstractCalendarClickActionDto {

    AbstractCalendarInlineContentDto content;
    List<CalendarFormularFieldOverrideDto> fieldOverrides;
    List<CalendarFormularSourceOverrideDto> sourceOverrides;
}
