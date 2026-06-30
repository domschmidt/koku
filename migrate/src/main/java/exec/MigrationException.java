package exec;

public class MigrationException extends RuntimeException {

    public MigrationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
