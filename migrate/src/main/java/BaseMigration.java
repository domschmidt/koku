import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public abstract class BaseMigration {

    protected final Connection source;
    protected final Connection target;

    public BaseMigration(Connection source, Connection target) {
        this.source = source;
        this.target = target;
    }

    protected void read(String query, Consumer<ResultSet> handler) throws SQLException {
        read(query, handler, this.source);
    }

    protected void read(String query, Consumer<ResultSet> handler, Connection customSource) throws SQLException {
        try (PreparedStatement stmt = customSource.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                handler.accept(rs);
            }
        }
    }

    protected void exec(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = target.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    public abstract void migrate() throws Exception;
}
