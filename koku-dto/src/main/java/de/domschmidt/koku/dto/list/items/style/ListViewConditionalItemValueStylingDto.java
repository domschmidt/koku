package de.domschmidt.koku.dto.list.items.style;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.style.AbstractListViewGlobalItemStylingDto;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("condition")
@SuperBuilder
@Data
public class ListViewConditionalItemValueStylingDto extends AbstractListViewGlobalItemStylingDto {

    String compareValuePath;
    @Singular
    List<Object> expectedValues = new ArrayList<>();
    ListViewItemStylingDto positiveStyling;
    ListViewItemStylingDto negativeStyling;

}

