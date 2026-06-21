package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("route-based-override")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class CalendarRouteBasedFormularFieldOverrideDto extends CalendarFormularContentOverrideDto {

    String routeParam;
}
