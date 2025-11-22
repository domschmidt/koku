package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@JsonTypeName("business-exception-with-confirmation-message")
public class KokuBusinessExceptionWithConfirmationMessageDto extends KokuBusinessException {

    String headline;
    String confirmationMessage;
    KokuBusinessExceptionButtonDto headerButton;
    Boolean closeOnClickOutside;

    @Singular
    List<KokuBusinessExceptionButtonDto> buttons;

}
