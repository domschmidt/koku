package de.domschmidt.list.dto.response.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("reload")
@SuperBuilder
@Data
public class ListViewReloadActionEvent extends AbstractListViewActionEventDto {
}
