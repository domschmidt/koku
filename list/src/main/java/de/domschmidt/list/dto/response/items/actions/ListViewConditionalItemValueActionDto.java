package de.domschmidt.list.dto.response.items.actions;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

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
