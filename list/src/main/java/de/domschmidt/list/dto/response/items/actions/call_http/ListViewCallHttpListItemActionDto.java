package de.domschmidt.list.dto.response.items.actions.call_http;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.ListViewUserConfirmationDto;
import de.domschmidt.list.dto.response.items.actions.AbstractListViewItemActionDto;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("http-call")
@SuperBuilder
@Data
public class ListViewCallHttpListItemActionDto extends AbstractListViewItemActionDto {

    String url;

    @Builder.Default
    List<AbstractListViewCallHttpListActionParamDto> params = new ArrayList<>();

    ListViewCallHttpListItemActionMethodEnumDto method;
    ListViewUserConfirmationDto userConfirmation;
}
