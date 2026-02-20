package exec;

import java.sql.Connection;

public class ActivityMigration extends BaseMigration {

    public ActivityMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating Activity...");

        read(
                "SELECT id, recorded, updated, approximately_duration, deleted, description FROM" + " koku.activity",
                rs -> {
                    try {
                        exec(
                                """
                        INSERT INTO koku.activity (external_ref, recorded, updated, approximately_duration, deleted, name)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?, ?)
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      name = EXCLUDED.name,
                                      version = activity.version + 1
                        WHERE EXCLUDED.updated > activity.updated;
                        """,
                                rs.getString("id"),
                                rs.getTimestamp("recorded"),
                                rs.getTimestamp("updated"),
                                rs.getTimestamp("updated"),
                                rs.getLong("approximately_duration"),
                                rs.getBoolean("deleted"),
                                rs.getString("description"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("✔ Activity done.");
    }
}
