package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@JsonTypeName("open-routed-inline-formular")
@Data
public class ListViewOpenRoutedInlineFormularContentSaveEventDto extends AbstractListViewItemInlineFormularContentSaveEventDto {

    String route;
    List<AbstractListViewInlineFormularContentOpenRoutedContentParamDto> params;

}
