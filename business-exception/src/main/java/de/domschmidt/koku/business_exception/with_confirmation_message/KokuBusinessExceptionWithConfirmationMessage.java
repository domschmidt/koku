package de.domschmidt.koku.business_exception.with_confirmation_message;

import de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionWithConfirmationMessageDto;
import lombok.Getter;

@Getter
public class KokuBusinessExceptionWithConfirmationMessage extends RuntimeException {

    private final KokuBusinessExceptionWithConfirmationMessageDto confirmationMessage;

    public KokuBusinessExceptionWithConfirmationMessage(
            final KokuBusinessExceptionWithConfirmationMessageDto confirmationMessage) {
        super(confirmationMessage.getConfirmationMessage());
        this.confirmationMessage = confirmationMessage;
    }
}
