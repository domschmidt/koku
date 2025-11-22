package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractCalendarRoutedContentDto {

    String route;
    String itemId;
    @Builder.Default
    List<AbstractCalendarRoutedContentGlobalEventListenerDto> globalEventListeners = new ArrayList<>();

}