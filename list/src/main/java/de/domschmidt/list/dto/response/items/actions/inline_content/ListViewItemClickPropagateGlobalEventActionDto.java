package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("propagate-global-event")
@SuperBuilder
@Data
public class ListViewItemClickPropagateGlobalEventActionDto extends AbstractListViewItemClickActionDto {

    String eventName;

}