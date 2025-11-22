package de.domschmidt.list.dto.response.inline_content.header;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeName("header")
@Data
public class ListViewHeaderContentDto extends AbstractListViewContentDto {

    String title;

    String sourceUrl;
    String titlePath;

    AbstractListViewContentDto content;

    @Builder.Default
    List<AbstractListViewInlineHeaderContentGlobalEventListenersDto> globalEventListeners = new ArrayList<>();

}
