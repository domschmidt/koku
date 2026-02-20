package de.domschmidt.list.dto.response.inline_content.list;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("list")
@Data
public class ListViewListContentDto extends AbstractListViewContentDto {

    String listUrl;
    String sourceUrl;
    Integer maxWidthInPx;
    Map<String, AbstractListViewListContentContextDto> context;
}
