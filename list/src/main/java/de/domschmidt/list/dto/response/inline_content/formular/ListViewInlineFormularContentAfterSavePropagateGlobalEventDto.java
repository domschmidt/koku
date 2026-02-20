package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
public class ListViewInlineFormularContentAfterSavePropagateGlobalEventDto
        extends AbstractListViewItemInlineFormularContentSaveEventDto {

    String eventName;
}
