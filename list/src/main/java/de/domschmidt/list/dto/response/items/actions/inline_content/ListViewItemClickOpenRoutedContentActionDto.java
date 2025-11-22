package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeName("open-routed-content")
@SuperBuilder
@Data
public class ListViewItemClickOpenRoutedContentActionDto extends AbstractListViewItemClickActionDto {

    String route;
    List<AbstractListViewItemClickOpenRoutedContentActionParamDto> params;

}