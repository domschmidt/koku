package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("item-value")
@Data
public class CalendarCallHttpItemActionItemValueParamDto extends AbstractCalendarCallHttpItemActionParamDto{

    String valuePath;

}
