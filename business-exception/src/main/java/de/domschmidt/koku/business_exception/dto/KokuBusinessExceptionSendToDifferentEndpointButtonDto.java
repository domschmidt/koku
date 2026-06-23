package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@JsonTypeName("send-to-different-endpoint-button")
public class KokuBusinessExceptionSendToDifferentEndpointButtonDto extends KokuBusinessExceptionButtonDto {

    KokuBusinessExceptionSendToDifferentEndpointMethodEnum endpointMethod;
    String endpointUrl;
    Boolean showLoadingAnimation;
    Boolean showDisabledState;
}
