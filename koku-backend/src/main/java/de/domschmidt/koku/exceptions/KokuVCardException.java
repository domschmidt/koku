package de.domschmidt.koku.exceptions;

import java.net.URISyntaxException;

public class KokuVCardException extends Exception {

    public KokuVCardException(final URISyntaxException use) {
        super(use);
    }
}
