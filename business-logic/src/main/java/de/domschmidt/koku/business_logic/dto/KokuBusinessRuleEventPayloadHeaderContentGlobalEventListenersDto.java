package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@JsonTypeName("event-payload")
@Data
@EqualsAndHashCode(callSuper = true)
public class KokuBusinessRuleEventPayloadHeaderContentGlobalEventListenersDto
        extends AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto {

    String idPath;
    String titleValuePath;
}
