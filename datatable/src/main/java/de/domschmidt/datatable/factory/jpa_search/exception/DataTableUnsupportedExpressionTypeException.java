package de.domschmidt.datatable.factory.jpa_search.exception;

public class DataTableUnsupportedExpressionTypeException extends Exception {

    public DataTableUnsupportedExpressionTypeException(
            final String message
    ) {
        super(message);
    }

    public DataTableUnsupportedExpressionTypeException(
            final String message,
            final Exception cause
    ) {
        super(message, cause);
    }
}
