package de.domschmidt.list.dto.response.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@SuperBuilder
@JsonTypeName("item-add")
@Data
public class ListViewEventPayloadAddItemGlobalEventListenerDto extends AbstractListViewGlobalEventListenerDto {

    String idPath;
    Map<String, ListViewReference> valueMapping;

}