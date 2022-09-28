package de.domschmidt.datatable.factory.exception;

public class DataTableException extends RuntimeException {

    public DataTableException(final Exception e) {
        super(e);
    }

    public DataTableException(final String msg) {
        super(msg);
    }

}
