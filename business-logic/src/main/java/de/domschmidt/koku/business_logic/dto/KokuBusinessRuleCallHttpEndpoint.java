package de.domschmidt.koku.business_logic.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@JsonTypeName("call-http-endpoint")
public class KokuBusinessRuleCallHttpEndpoint extends AbstractKokuBusinessRuleExecutionDto {

    String url;
    KokuBusinessRuleCallHttpEndpointMethodEnum method;
}
