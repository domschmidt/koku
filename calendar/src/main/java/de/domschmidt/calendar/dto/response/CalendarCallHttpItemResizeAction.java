package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("call-http")
@Getter
public class CalendarCallHttpItemResizeAction extends AbstractCalendarItemResizeAction {

    String endDatePath;
    String endTimePath;
    String url;
    CalendarCallHttpItemActionMethodEnum method;

    @Builder.Default
    List<AbstractCalendarCallHttpItemActionParamDto> urlParams = new ArrayList<>();

    @Builder.Default
    Map<String, String> valueMapping = new HashMap<>();

    @Builder.Default
    List<AbstractCalendarCallHttpItemActionSuccessEventDto> successEvents = new ArrayList<>();
}
