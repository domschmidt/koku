package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonTypeName("global-event-listener")
@Builder
public class KokuBusinessRuleOpenContentCloseGlobalEventListenerDto
        extends AbstractKokuBusinessRuleOpenContentCloseListenerDto {

    String eventName;
}
