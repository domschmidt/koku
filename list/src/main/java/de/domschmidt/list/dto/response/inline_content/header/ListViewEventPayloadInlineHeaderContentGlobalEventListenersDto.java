package de.domschmidt.list.dto.response.inline_content.header;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class ListViewEventPayloadInlineHeaderContentGlobalEventListenersDto
        extends AbstractListViewInlineHeaderContentGlobalEventListenersDto {

    String idPath;
    String titleValuePath;
}
