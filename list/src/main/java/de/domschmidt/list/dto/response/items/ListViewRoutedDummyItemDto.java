package de.domschmidt.list.dto.response.items;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("routed-item")
@Data
public class ListViewRoutedDummyItemDto extends AbstractListViewRoutedItemDto {

    String text;

}
