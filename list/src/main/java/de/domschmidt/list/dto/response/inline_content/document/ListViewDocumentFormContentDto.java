package de.domschmidt.list.dto.response.inline_content.document;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("document-form")
@Data
public class ListViewDocumentFormContentDto extends AbstractListViewContentDto {

    String documentUrl;
    String submitUrl;
    List<AbstractListViewDocumentFormContentSubmitEventDto> onSubmitEvents;

}
