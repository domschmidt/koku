package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.actions.AbstractListViewItemActionDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeName("open-routed-content")
@SuperBuilder
@Data
public class ListViewItemActionOpenRoutedContentActionDto extends AbstractListViewItemActionDto {

    String route;
    List<AbstractListViewItemActionOpenRoutedContentActionParamDto> params;

}