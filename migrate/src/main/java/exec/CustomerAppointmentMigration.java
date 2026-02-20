package exec;

import java.sql.Connection;
import java.util.Map;

public class CustomerAppointmentMigration extends BaseMigration {

    private final Map<String, String> userMapping;

    public CustomerAppointmentMigration(Connection source, Connection target, Map<String, String> userMapping) {
        super(source, target);
        this.userMapping = userMapping;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating CustomerAppointment...");

        read(
                "SELECT id, recorded, updated, additional_info, description, start, customer_id, user_id"
                        + " FROM koku.customer_appointment",
                rs -> {
                    try {
                        String originUserId = rs.getString("user_id");
                        String mappedUserId = this.userMapping.get(originUserId);
                        if (mappedUserId != null) {
                            exec(
                                    """
                                    INSERT INTO koku.customer_appointment (external_ref, recorded, updated, deleted, additional_info, description, start, customer_id, user_id)
                                    VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, COALESCE(?, ''), COALESCE(?, ''), ?, (SELECT ID FROM koku.customer where external_ref = ?), ?)
                                    ON CONFLICT (external_ref)
                                    DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                                  updated = EXCLUDED.updated,
                                                  deleted = EXCLUDED.deleted,
                                                  additional_info = EXCLUDED.additional_info,
                                                  description = EXCLUDED.description,
                                                  start = EXCLUDED.start,
                                                  customer_id = EXCLUDED.customer_id,
                                                  user_id = EXCLUDED.user_id,
                                                  version = customer_appointment.version + 1
                                    WHERE EXCLUDED.updated > customer_appointment.updated;
                                    """,
                                    rs.getString("id"),
                                    rs.getTimestamp("recorded"),
                                    rs.getTimestamp("updated"),
                                    rs.getTimestamp("updated"),
                                    false,
                                    rs.getString("additional_info"),
                                    rs.getString("description"),
                                    rs.getTimestamp("start"),
                                    rs.getString("customer_id"),
                                    mappedUserId);
                        } else {
                            System.err.printf("%s is not available in user mapping", originUserId);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("✔ CustomerAppointment done.");
    }
}
