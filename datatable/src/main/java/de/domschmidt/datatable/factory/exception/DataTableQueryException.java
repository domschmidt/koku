package de.domschmidt.datatable.factory.exception;

public class DataTableQueryException extends RuntimeException {

    public DataTableQueryException(final String msg) {
        super(msg);
    }

    public DataTableQueryException(final String msg, final Exception cause) {
        super(msg, cause);
    }
}
