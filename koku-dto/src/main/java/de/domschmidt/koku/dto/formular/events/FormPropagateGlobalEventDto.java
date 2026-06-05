package de.domschmidt.koku.dto.formular.events;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@JsonTypeName("propagate-global-event")
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
public class FormPropagateGlobalEventDto extends AbstractFormEventDto {

    String eventName;
}
