package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@JsonTypeName("value")
@SuperBuilder
@Data
public class FormNotificationEventValueParamDto extends AbstractFormNotificationEventParamDto {

    String sourcePath;
}
