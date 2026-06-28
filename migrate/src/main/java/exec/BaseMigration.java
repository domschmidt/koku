package exec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class BaseMigration {

    protected final Connection source;
    protected final Connection target;
    private final Logger logger = Logger.getLogger(getClass().getName());

    protected BaseMigration(Connection source, Connection target) {
        this.source = source;
        this.target = target;
    }

    protected void read(String query, Consumer<ResultSet> handler) {
        read(query, handler, this.source);
    }

    protected void read(String query, Consumer<ResultSet> handler, Connection customSource) {
        try (PreparedStatement stmt = customSource.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                handler.accept(rs);
            }
        } catch (final SQLException exception) {
            throw new IllegalStateException("Unable to execute migration read query", exception);
        }
    }

    protected void exec(String sql, Object... params) {
        try (PreparedStatement stmt = target.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (final SQLException exception) {
            throw new IllegalStateException("Unable to execute migration statement", exception);
        }
    }

    protected void logInfo(String message) {
        this.logger.info(message);
    }

    protected void logWarning(String message) {
        this.logger.warning(message);
    }

    public abstract void migrate();
}
