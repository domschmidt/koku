import java.sql.Connection;

public class ActivityStepMigration extends BaseMigration {

    public ActivityStepMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating ActivityStep...");

        read("SELECT id, recorded, updated, deleted, description FROM koku.activity_step", rs -> {
            try {
                exec("""
                        INSERT INTO koku.activity_step (external_ref, recorded, updated, deleted, name)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?)
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      name = EXCLUDED.name,
                                      version = activity_step.version + 1
                        WHERE EXCLUDED.updated > activity_step.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBoolean("deleted"),
                        rs.getString("description")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ ActivityStep done.");
    }
}
