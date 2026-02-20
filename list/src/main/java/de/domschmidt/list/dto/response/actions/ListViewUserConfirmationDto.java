package de.domschmidt.list.dto.response.actions;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ListViewUserConfirmationDto {

    String headline;
    String content;
    List<AbstractListViewUserConfirmationParamDto> params = new ArrayList<>();
}
