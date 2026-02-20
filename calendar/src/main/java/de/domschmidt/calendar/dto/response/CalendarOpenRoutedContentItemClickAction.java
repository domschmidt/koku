package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Getter
public class CalendarOpenRoutedContentItemClickAction extends AbstractCalendarItemClickAction {

    String route;

    @Builder.Default
    List<AbstractCalendarOpenRoutedContentItemParamDto> params = new ArrayList<>();
}
