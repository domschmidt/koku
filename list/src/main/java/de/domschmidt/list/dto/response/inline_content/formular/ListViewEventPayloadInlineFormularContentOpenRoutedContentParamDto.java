package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class ListViewEventPayloadInlineFormularContentOpenRoutedContentParamDto
        extends AbstractListViewInlineFormularContentOpenRoutedContentParamDto {

    String valuePath;
}
