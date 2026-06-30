package de.domschmidt.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("item-value")
@Data
@EqualsAndHashCode(callSuper = true)
public class ItemValueCalendarOpenRoutedContentItemParamDto extends AbstractCalendarOpenRoutedContentItemParamDto {

    String valuePath;
}
