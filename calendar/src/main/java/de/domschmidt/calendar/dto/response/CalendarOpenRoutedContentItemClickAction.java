package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Getter
public class CalendarOpenRoutedContentItemClickAction extends AbstractCalendarItemClickAction {

    String route;
    @Builder.Default
    List<AbstractCalendarOpenRoutedContentItemParamDto> params = new ArrayList<>();

}
