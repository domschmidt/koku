package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("dock")
@Data
public class CalendarDockInlineContentDto extends AbstractCalendarInlineContentDto {

    @Builder.Default
    List<CalendarInlineDockContentItemDto> content = new ArrayList<>();

}
