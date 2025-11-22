package de.domschmidt.list.dto.response.inline_content.document;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class ListViewEventPayloadDocumentFormOpenRoutedContentParamDto extends AbstractListViewInlineDocumentFormAfterSaveParamDto {

    String valuePath;

}
