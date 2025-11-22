package de.domschmidt.list.dto.response.inline_content.dock;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("dock")
@Data
public class ListViewDockContentDto extends AbstractListViewContentDto {

    @Builder.Default
    List<ListViewItemInlineDockContentItemDto> content = new ArrayList<>();

}
