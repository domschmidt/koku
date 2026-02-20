package de.domschmidt.list.dto.response.inline_content.dock;

import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ListViewItemInlineDockContentItemDto {

    String id;
    String title;
    String route;
    String icon;
    AbstractListViewContentDto content;
}
