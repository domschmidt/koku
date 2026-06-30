package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-content")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarOpenContentClickActionDto extends AbstractCalendarClickActionDto {

    AbstractCalendarInlineContentDto content;
    List<CalendarFormularContentOverrideDto> contentOverrides;
    List<CalendarFormularSourceOverrideDto> sourceOverrides;
}
