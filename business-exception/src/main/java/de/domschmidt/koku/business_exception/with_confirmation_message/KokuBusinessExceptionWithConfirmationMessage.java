package de.domschmidt.koku.business_exception.with_confirmation_message;

import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import lombok.Getter;

@Getter
public class KokuBusinessExceptionWithConfirmationMessage extends RuntimeException {

    private final transient KokuBusinessErrorWithConfirmationMessageDto confirmationMessage;

    public KokuBusinessExceptionWithConfirmationMessage(
            final KokuBusinessErrorWithConfirmationMessageDto confirmationMessage) {
        super(confirmationMessage.getConfirmationMessage());
        this.confirmationMessage = confirmationMessage;
    }
}
