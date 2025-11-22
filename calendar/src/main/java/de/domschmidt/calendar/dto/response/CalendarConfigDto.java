package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class CalendarConfigDto {

    List<AbstractCalendarListSourceConfigDto> listSources = new ArrayList<>();
    List<AbstractCalendarActionDto> calendarActions = new ArrayList<>();
    AbstractCalendarClickActionDto calendarClickAction;
    List<AbstractCalendarGlobalEventListenerDto> globalEventListeners = new ArrayList<>();

    @Builder.Default
    List<AbstractCalendarRoutedContentDto> routedContents = new ArrayList<>();

}
