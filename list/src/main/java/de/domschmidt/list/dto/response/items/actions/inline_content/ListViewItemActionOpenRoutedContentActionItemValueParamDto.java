package de.domschmidt.list.dto.response.items.actions.inline_content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("value")
public class ListViewItemActionOpenRoutedContentActionItemValueParamDto
        extends AbstractListViewItemActionOpenRoutedContentActionParamDto {

    ListViewReference valueReference;
}
