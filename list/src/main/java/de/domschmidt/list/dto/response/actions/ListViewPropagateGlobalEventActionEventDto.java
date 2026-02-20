package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("propagate-global-event")
@SuperBuilder
@Data
public class ListViewPropagateGlobalEventActionEventDto extends AbstractListViewActionEventDto {

    String eventName;
}
