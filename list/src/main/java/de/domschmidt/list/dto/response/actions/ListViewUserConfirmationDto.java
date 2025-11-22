package de.domschmidt.list.dto.response.actions;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class ListViewUserConfirmationDto {

    String headline;
    String content;
    List<AbstractListViewUserConfirmationParamDto> params = new ArrayList<>();

}
