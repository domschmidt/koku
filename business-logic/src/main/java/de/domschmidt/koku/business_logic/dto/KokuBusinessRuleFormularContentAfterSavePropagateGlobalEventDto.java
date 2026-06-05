package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
@EqualsAndHashCode(callSuper = true)
public class KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto
        extends AbstractKokuBusinessRuleFormularContentSaveEventDto {

    String eventName;
}
