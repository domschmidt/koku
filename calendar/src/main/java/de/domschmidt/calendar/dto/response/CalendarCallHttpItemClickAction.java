package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuperBuilder
@JsonTypeName("call-http")
@Getter
public class CalendarCallHttpItemClickAction extends AbstractCalendarItemClickAction {

    String startDatePath;
    String startTimePath;
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
