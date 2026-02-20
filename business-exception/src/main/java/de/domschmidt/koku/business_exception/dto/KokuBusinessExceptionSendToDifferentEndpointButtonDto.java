package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonTypeName("send-to-different-endpoint-button")
public class KokuBusinessExceptionSendToDifferentEndpointButtonDto extends KokuBusinessExceptionButtonDto {

    KokuBusinessExceptionSendToDifferentEndpointMethodEnum endpointMethod;
    String endpointUrl;
    Boolean showLoadingAnimation;
    Boolean showDisabledState;
}
