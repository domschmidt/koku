package de.domschmidt.koku.business_logic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KokuBusinessRuleFieldReferenceListenerDto {

    KokuBusinessRuleFieldReferenceListenerEventEnum event;
}
