package de.domschmidt.list.dto.response.items.actions;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.domschmidt.list.dto.response.actions.AbstractListViewActionEventDto;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractListViewItemActionDto {

    String icon;
    @Builder.Default
    List<AbstractListViewActionEventDto> successEvents = new ArrayList<>();
    @Builder.Default
    List<AbstractListViewActionEventDto> failEvents = new ArrayList<>();

}
