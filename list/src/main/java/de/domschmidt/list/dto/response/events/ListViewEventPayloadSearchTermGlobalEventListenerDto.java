package de.domschmidt.list.dto.response.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload-search-term")
@Data
public class ListViewEventPayloadSearchTermGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {

    String valuePath;
}
