package exec;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CustomerAppointmentActivitySequenceMigration extends BaseMigration {

    private final Connection activityStepTarget;
    private final Connection productTarget;

    public CustomerAppointmentActivitySequenceMigration(
            Connection source, Connection target, Connection activityStepTarget, Connection productTarget) {
        super(source, target);
        this.activityStepTarget = activityStepTarget;
        this.productTarget = productTarget;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating CustomerAppointmentActivitySequence...");

        Map<String, Long> activityStepExternalRefMapping = new HashMap<>();
        read(
                "SELECT id, external_ref FROM koku.activity_step",
                rs -> {
                    try {
                        activityStepExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                this.activityStepTarget);
        Map<String, Long> productTargetExternalRefMapping = new HashMap<>();
        read(
                "SELECT id, external_ref FROM koku.product",
                rs -> {
                    try {
                        productTargetExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                },
                this.productTarget);

        read(
                "SELECT id, recorded, updated, customer_appointment_id, optional_activity_step_id,"
                        + " optional_product_id, position FROM koku.activity_sequence_item",
                rs -> {
                    try {
                        String activityStepIdRaw = rs.getString("optional_activity_step_id");
                        String productIdRaw = rs.getString("optional_product_id");
                        exec(
                                """
                                INSERT INTO koku.customer_appointment_activity_sequence (external_ref, recorded, updated, appointment_id, activity_step_id, product_id, position)
                                VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, (SELECT ID FROM koku.customer_appointment where external_ref = ?), ?, ?, ?)
                                ON CONFLICT (external_ref)
                                DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                              updated = EXCLUDED.updated,
                                              appointment_id = EXCLUDED.appointment_id,
                                              activity_step_id = EXCLUDED.activity_step_id,
                                              product_id = EXCLUDED.product_id,
                                              position = EXCLUDED.position,
                                              version = customer_appointment_activity_sequence.version + 1
                                WHERE EXCLUDED.updated > customer_appointment_activity_sequence.updated;
                                """,
                                rs.getString("id"),
                                rs.getTimestamp("recorded"),
                                rs.getTimestamp("updated"),
                                rs.getTimestamp("updated"),
                                rs.getString("customer_appointment_id"),
                                activityStepExternalRefMapping.get(activityStepIdRaw),
                                productTargetExternalRefMapping.get(productIdRaw),
                                rs.getInt("position"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        System.out.println("✔ CustomerAppointmentActivitySequence done.");
    }
}
