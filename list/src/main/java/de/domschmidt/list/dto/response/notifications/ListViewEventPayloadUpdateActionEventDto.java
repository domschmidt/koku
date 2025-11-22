package de.domschmidt.list.dto.response.notifications;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import de.domschmidt.list.dto.response.actions.AbstractListViewActionEventDto;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

@JsonTypeName("event-payload-update")
@SuperBuilder
@Data
public class ListViewEventPayloadUpdateActionEventDto extends AbstractListViewActionEventDto {

    String idPath;
    Map<String, ListViewReference> valueMapping = new HashMap<>();

}