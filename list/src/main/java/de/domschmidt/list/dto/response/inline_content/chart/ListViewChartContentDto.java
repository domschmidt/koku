package de.domschmidt.list.dto.response.inline_content.chart;

import com.fasterxml.jackson.annotation.JsonTypeName;
import de.domschmidt.list.dto.response.actions.AbstractListViewContentDto;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("chart")
@Getter
public class ListViewChartContentDto extends AbstractListViewContentDto {

    String chartUrl;

}
