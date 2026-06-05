package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("date-value")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class FormNotificationEventDateValueParamDto extends AbstractFormNotificationEventParamDto {

    String sourcePath;
}
