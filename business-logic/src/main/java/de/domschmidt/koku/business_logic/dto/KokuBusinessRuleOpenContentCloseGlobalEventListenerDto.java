package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@JsonTypeName("global-event-listener")
@Builder
public class KokuBusinessRuleOpenContentCloseGlobalEventListenerDto
        extends AbstractKokuBusinessRuleOpenContentCloseListenerDto {

    String eventName;
}
