package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("propagate-global-event")
@Data
public class KokuBusinessRuleFormularContentAfterSavePropagateGlobalEventDto extends AbstractKokuBusinessRuleFormularContentSaveEventDto {

    String eventName;

}
