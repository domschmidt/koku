package de.domschmidt.koku.business_exception.with_confirmation_message;

import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalKokuBusinessExceptionWithConfirmationMessageHandler {

    @ExceptionHandler(KokuBusinessExceptionWithConfirmationMessage.class)
    public ResponseEntity<KokuBusinessErrorWithConfirmationMessageDto> handleConflict(
            KokuBusinessExceptionWithConfirmationMessage exception) {
        return new ResponseEntity<>(exception.getConfirmationMessage(), HttpStatus.CONFLICT);
    }
}
