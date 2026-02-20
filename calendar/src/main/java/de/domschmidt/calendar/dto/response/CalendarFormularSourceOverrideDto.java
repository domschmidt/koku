package de.domschmidt.calendar.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CalendarFormularSourceOverrideDto {

    String sourcePath;
    CalendarFormularContextSourceValueEnumDto value;
    Long offsetValue;
    CalendarFormularContextSourceOffsetUnitEnumDto offsetUnit;
}
