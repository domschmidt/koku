package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import de.domschmidt.list.dto.response.items.AbstractListViewItemClickActionDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("open-inline-content")
@SuperBuilder
@Data
public class ListViewItemOpenInlineContentClickActionDto extends AbstractListViewItemClickActionDto {

    AbstractListViewContentDto inlineContent;
    String headerTitle;
}
