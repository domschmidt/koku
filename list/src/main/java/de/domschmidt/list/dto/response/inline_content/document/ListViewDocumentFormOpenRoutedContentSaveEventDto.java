package de.domschmidt.list.dto.response.inline_content.document;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("open-routed-content")
@Data
public class ListViewDocumentFormOpenRoutedContentSaveEventDto
        extends AbstractListViewDocumentFormContentDtoSaveEventDto {

    String route;
    List<AbstractListViewInlineDocumentFormAfterSaveParamDto> params;
}
