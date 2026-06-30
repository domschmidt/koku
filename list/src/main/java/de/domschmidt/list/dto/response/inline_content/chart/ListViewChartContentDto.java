package de.domschmidt.list.dto.response.inline_content.chart;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("chart")
@EqualsAndHashCode(callSuper = true)
@Getter
public class ListViewChartContentDto extends AbstractListViewContentDto {

    String chartUrl;
}
