package de.domschmidt.list.dto.response.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class ListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto extends AbstractListViewEventPayloadOpenRoutedContentGlobalEventListenerParamDto {

    String param;
    String valuePath;

}
