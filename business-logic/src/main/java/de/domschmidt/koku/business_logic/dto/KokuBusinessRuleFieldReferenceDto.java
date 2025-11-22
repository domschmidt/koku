package de.domschmidt.koku.business_logic.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class KokuBusinessRuleFieldReferenceDto {

    String reference;
    String requestParam;
    String resultValuePath;
    Boolean loadingAnimation;
    KokuBusinessRuleFieldReferenceUpdateModeEnum resultUpdateMode;
    @Singular
    private final List<KokuBusinessRuleFieldReferenceListenerDto> listeners;

}
