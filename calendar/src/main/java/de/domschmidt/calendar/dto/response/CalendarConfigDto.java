package de.domschmidt.calendar.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class CalendarConfigDto {

    String id;
    List<AbstractCalendarListSourceConfigDto> listSources = new ArrayList<>();
    List<AbstractCalendarActionDto> calendarActions = new ArrayList<>();
    AbstractCalendarClickActionDto calendarClickAction;
    List<AbstractCalendarGlobalEventListenerDto> globalEventListeners = new ArrayList<>();

    @Builder.Default
    List<AbstractCalendarRoutedContentDto> routedContents = new ArrayList<>();
}
