package de.domschmidt.list.dto.response.inline_content.header;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("item-value")
@SuperBuilder
@Data
public class ListViewInlineHeaderContentParamDto extends AbstractListViewInlineHeaderContentParamDto {

    String valuePath;
}
