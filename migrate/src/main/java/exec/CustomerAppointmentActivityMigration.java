package exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CustomerAppointmentActivityMigration extends BaseMigration {

    private final Connection activityTarget;

    public CustomerAppointmentActivityMigration(Connection source, Connection target, Connection activityTarget) {
        super(source, target);
        this.activityTarget = activityTarget;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating CustomerAppointmentActivity...");

        Map<String , Long> activityExternalRefMapping = new HashMap<>();
        read("SELECT id, external_ref FROM koku.activity", rs -> {
            try {
                activityExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, this.activityTarget);

        read("SELECT id, recorded, updated, position, sell_price, activity_id, customer_appointment_id FROM koku.customer_appointment_activities_composing", rs -> {
            try {
                String activityIdRaw = rs.getString("activity_id");
                if (!activityExternalRefMapping.containsKey(activityIdRaw)) {
                    System.out.printf("Missing activity_id %s%n", activityIdRaw);
                }
                exec("""
                                INSERT INTO koku.customer_appointment_activity (external_ref, recorded, updated, position, sell_price, activity_id, appointment_id)
                                VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?, ?, (SELECT ID FROM koku.customer_appointment where external_ref = ?))
                                ON CONFLICT (external_ref)
                                DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                              updated = EXCLUDED.updated,
                                              position = EXCLUDED.position,
                                              sell_price = EXCLUDED.sell_price,
                                              activity_id = EXCLUDED.activity_id,
                                              appointment_id = EXCLUDED.appointment_id,
                                              version = customer_appointment_activity.version + 1
                                WHERE EXCLUDED.updated > customer_appointment_activity.updated;
                                """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getInt("position"),
                        rs.getBigDecimal("sell_price"),
                        activityExternalRefMapping.get(activityIdRaw),
                        rs.getString("customer_appointment_id")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ CustomerAppointmentActivity done.");
    }
}
