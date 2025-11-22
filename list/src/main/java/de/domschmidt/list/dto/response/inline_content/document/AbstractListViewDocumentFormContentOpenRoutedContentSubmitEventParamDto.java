package de.domschmidt.list.dto.response.inline_content.document;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@Data
public abstract class AbstractListViewDocumentFormContentOpenRoutedContentSubmitEventParamDto {

    String param;

}