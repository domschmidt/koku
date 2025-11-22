package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("file-viewer")
@Data
public class ListViewFileViewerContentDto extends AbstractListViewContentDto {

    String sourceUrl;
    String fileUrl;
    String mimeTypeSourcePath;

}
