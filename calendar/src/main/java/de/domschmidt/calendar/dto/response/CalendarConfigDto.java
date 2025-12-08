package de.domschmidt.calendar.dto.response;


import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
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
