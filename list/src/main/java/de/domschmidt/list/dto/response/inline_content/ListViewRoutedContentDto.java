package de.domschmidt.list.dto.response.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import de.domschmidt.list.dto.response.actions.AbstractListViewRoutedContentDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("routed-inline-content")
@Data
public class ListViewRoutedContentDto extends AbstractListViewRoutedContentDto {

    AbstractListViewContentDto inlineContent;
    AbstractListViewContentDto modalContent;
}
