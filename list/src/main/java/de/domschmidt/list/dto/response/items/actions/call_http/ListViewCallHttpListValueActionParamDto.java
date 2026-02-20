package de.domschmidt.list.dto.response.items.actions.call_http;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.ListViewReference;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("value")
@SuperBuilder
@Data
public class ListViewCallHttpListValueActionParamDto extends AbstractListViewCallHttpListActionParamDto {

    ListViewReference valueReference;
}
