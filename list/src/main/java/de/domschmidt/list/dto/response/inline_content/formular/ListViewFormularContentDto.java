package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import de.domschmidt.list.dto.response.items.actions.ListViewFormularActionSubmitMethodEnumDto;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("formular")
@Data
public class ListViewFormularContentDto extends AbstractListViewContentDto {

    String formularUrl;
    String sourceUrl;
    String submitUrl;
    ListViewFormularActionSubmitMethodEnumDto submitMethod;
    Integer maxWidthInPx;
    List<AbstractListViewItemInlineFormularContentSaveEventDto> onSaveEvents;
    List<ListViewFormularFieldOverrideDto> fieldOverrides;
}
