package de.domschmidt.list.dto.response.inline_content.formular;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractListViewInlineFormularContentOpenRoutedContentParamDto {

    String param;
}
