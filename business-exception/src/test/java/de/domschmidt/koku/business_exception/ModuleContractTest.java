package de.domschmidt.koku.business_exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.domschmidt.koku.business_exception.dto.KokuBusinessErrorWithConfirmationMessageDto;
import de.domschmidt.koku.business_exception.with_confirmation_message.GlobalKokuBusinessExceptionWithConfirmationMessageHandler;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ModuleContractTest {
    @Test
    void enumContractsExposeConstants() {
        assertTrue(
                de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionSendToDifferentEndpointMethodEnum
                                .values()
                                .length
                        > 0);
        assertTrue(de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionButtonStyle.values().length > 0);
        assertTrue(de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionButtonSizeEnum.values().length > 0);
        assertTrue(de.domschmidt.koku.business_exception.dto.KokuBusinessExceptionButtonKeyListenerEnum.values().length
                > 0);
    }

    @Test
    void confirmationExceptionAndHandlerPreservePayload() {
        final KokuBusinessErrorWithConfirmationMessageDto payload =
                KokuBusinessErrorWithConfirmationMessageDto.builder()
                        .confirmationMessage("Confirm?")
                        .build();
        final KokuBusinessExceptionWithConfirmationMessage exception =
                new KokuBusinessExceptionWithConfirmationMessage(payload);

        assertEquals("Confirm?", exception.getMessage());
        assertSame(payload, exception.getConfirmationMessage());
        final var response = new GlobalKokuBusinessExceptionWithConfirmationMessageHandler().handleConflict(exception);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertSame(payload, response.getBody());
    }
}
