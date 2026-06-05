package de.domschmidt.koku.dto.list.items.style;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.items.style.AbstractListViewGlobalItemStylingDto;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@JsonTypeName("condition")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class ListViewConditionalItemValueStylingDto extends AbstractListViewGlobalItemStylingDto {

    String compareValuePath;

    @Singular
    List<Object> expectedValues;

    ListViewItemStylingDto positiveStyling;
    ListViewItemStylingDto negativeStyling;
}
