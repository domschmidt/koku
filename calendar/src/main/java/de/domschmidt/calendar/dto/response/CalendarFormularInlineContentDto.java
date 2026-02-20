package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("formular")
@Data
public class CalendarFormularInlineContentDto extends AbstractCalendarInlineContentDto {

    String formularUrl;
    String sourceUrl;
    String submitUrl;
    CalendarFormularActionSubmitMethodEnumDto submitMethod;
    Integer maxWidthInPx;
    List<AbstractCalendarItemInlineFormularContentSaveEventDto> onSaveEvents;
    List<CalendarFormularFieldOverrideDto> fieldOverrides;
    List<CalendarFormularSourceOverrideDto> sourceOverrides;
}
