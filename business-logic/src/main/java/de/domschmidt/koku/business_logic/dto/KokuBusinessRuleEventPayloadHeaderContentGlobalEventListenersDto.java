package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;


@SuperBuilder
@JsonTypeName("event-payload")
@Data
public class KokuBusinessRuleEventPayloadHeaderContentGlobalEventListenersDto extends AbstractKokuBusinessRuleHeaderContentGlobalEventListenersDto {

    String idPath;
    String titleValuePath;

}