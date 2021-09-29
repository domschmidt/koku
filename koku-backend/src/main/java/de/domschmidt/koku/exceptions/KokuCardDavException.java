package de.domschmidt.koku.exceptions;

public class KokuCardDavException extends Exception {
    public KokuCardDavException(final Exception e) {
        super(e);
    }

    public KokuCardDavException(String msg) {
        super(msg);
    }
}
