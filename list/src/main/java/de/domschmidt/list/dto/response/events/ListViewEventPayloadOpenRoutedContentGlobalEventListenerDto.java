package de.domschmidt.list.dto.response.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Data
public class ListViewEventPayloadOpenRoutedContentGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {

    String route;
    List<AbstractListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto> params;

}