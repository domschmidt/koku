package de.domschmidt.koku.business_logic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class KokuBusinessRuleDto {

    String id;
    @Singular
    private final List<KokuBusinessRuleFieldReferenceDto> references;
    private final AbstractKokuBusinessRuleExecutionDto execution;

}
