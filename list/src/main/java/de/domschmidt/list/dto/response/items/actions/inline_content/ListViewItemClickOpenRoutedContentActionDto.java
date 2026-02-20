package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import java.util.List;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-routed-content")
@SuperBuilder
@Data
public class ListViewItemClickOpenRoutedContentActionDto extends AbstractListViewItemClickActionDto {

    String route;
    List<AbstractListViewItemClickOpenRoutedContentActionParamDto> params;
}
