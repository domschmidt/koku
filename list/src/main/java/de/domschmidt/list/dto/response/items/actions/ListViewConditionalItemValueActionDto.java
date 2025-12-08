package de.domschmidt.list.dto.response.items.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonTypeName("condition")
@SuperBuilder
@Data
public class ListViewConditionalItemValueActionDto extends AbstractListViewItemActionDto {

    String compareValuePath;
    @Singular
    List<Object> expectedValues;
    AbstractListViewItemActionDto positiveAction;
    AbstractListViewItemActionDto negativeAction;

}
