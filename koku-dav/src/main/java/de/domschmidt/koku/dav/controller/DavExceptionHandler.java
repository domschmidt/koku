package de.domschmidt.koku.dav.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
class DavExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Void> responseStatusException(final ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).build();
    }
}
