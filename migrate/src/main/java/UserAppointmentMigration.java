import java.sql.Connection;

public class UserAppointmentMigration extends BaseMigration {

    public UserAppointmentMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating UserAppointment...");

        read("SELECT id, recorded, updated, deleted, description, start, user_id, ending FROM koku.private_appointment", rs -> {
            try {
                exec("""
                        INSERT INTO koku.user_appointment (external_ref, recorded, updated, deleted, description, start_timestamp, end_timestamp, user_id)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, COALESCE(?, ''), ?, ?, (SELECT ID FROM koku.user where external_ref = ?))
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      description = EXCLUDED.description,
                                      start_timestamp = EXCLUDED.start_timestamp,
                                      end_timestamp = EXCLUDED.end_timestamp,
                                      user_id = EXCLUDED.user_id,
                                      version = user_appointment.version + 1
                        WHERE EXCLUDED.updated > user_appointment.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBoolean("deleted"),
                        rs.getString("description"),
                        rs.getTimestamp("start"),
                        rs.getTimestamp("ending") != null ? rs.getTimestamp("ending") : rs.getTimestamp("start"),
                        rs.getString("user_id")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ UserAppointment done.");
    }
}
