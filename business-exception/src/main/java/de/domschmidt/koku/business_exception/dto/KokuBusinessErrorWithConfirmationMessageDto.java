package de.domschmidt.koku.business_exception.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@JsonTypeName("business-error-with-confirmation-message")
public class KokuBusinessErrorWithConfirmationMessageDto extends KokuBusinessError {

    String headline;
    String confirmationMessage;
    KokuBusinessExceptionButtonDto headerButton;
    Boolean closeOnClickOutside;

    @Singular
    List<KokuBusinessExceptionButtonDto> buttons;
}
