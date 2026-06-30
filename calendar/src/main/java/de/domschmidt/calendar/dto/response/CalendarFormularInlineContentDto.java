package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("formular")
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarFormularInlineContentDto extends AbstractCalendarInlineContentDto {

    String formularUrl;
    String sourceUrl;
    String submitUrl;
    CalendarFormularActionSubmitMethodEnumDto submitMethod;
    Integer maxWidthInPx;
    List<AbstractCalendarItemInlineFormularContentSaveEventDto> onSaveEvents;
    List<CalendarFormularContentOverrideDto> contentOverrides;
    List<CalendarFormularSourceOverrideDto> sourceOverrides;
}
