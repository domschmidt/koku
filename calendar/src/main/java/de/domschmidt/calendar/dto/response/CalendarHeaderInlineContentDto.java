package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

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