package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("header")
@Data
public class CalendarHeaderInlineContentDto extends AbstractCalendarInlineContentDto {

    String title;

    String sourceUrl;
    String titlePath;

    AbstractCalendarInlineContentDto content;

    @Builder.Default
    List<AbstractCalendarHeaderInlineContentGlobalEventListenersDto> globalEventListeners = new ArrayList<>();
}
