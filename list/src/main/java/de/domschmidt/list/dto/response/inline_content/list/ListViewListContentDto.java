package de.domschmidt.list.dto.response.inline_content.list;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@JsonTypeName("list")
@Data
public class ListViewListContentDto extends AbstractListViewContentDto {

    String listUrl;
    String sourceUrl;
    Integer maxWidthInPx;
    Map<String, AbstractListViewListContentContextDto> context;

}
