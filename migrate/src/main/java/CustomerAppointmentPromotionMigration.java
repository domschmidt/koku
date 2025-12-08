import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CustomerAppointmentPromotionMigration extends BaseMigration {
    private final Connection promotionTarget;

    public CustomerAppointmentPromotionMigration(Connection source, Connection target, Connection promotionTarget) {
        super(source, target);
        this.promotionTarget = promotionTarget;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating CustomerAppointmentPromotion...");
        Map<String, Long> promotionTargetExternalRefMapping = new HashMap<>();
        read("SELECT id, external_ref FROM koku.promotion", rs -> {
            try {
                promotionTargetExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, this.promotionTarget);

        read("SELECT customer_appointment_id, promotion_id, promotions_order FROM koku.customer_appointment_promotions_composing", rs -> {
            try {
                String promotionIdRaw = rs.getString("promotion_id");
                exec("""
                                INSERT INTO koku.customer_appointment_promotion (external_ref, recorded, updated, appointment_id, promotion_id, position)
                                VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT ID FROM koku.customer_appointment where external_ref = ?), ?, ?)
                                ON CONFLICT (external_ref)
                                DO UPDATE SET updated = CURRENT_TIMESTAMP,
                                              appointment_id = EXCLUDED.appointment_id,
                                              promotion_id = EXCLUDED.promotion_id,
                                              version = customer_appointment_promotion.version + 1
                                WHERE EXCLUDED.updated > customer_appointment_promotion.updated;
                                """,
                        rs.getString("customer_appointment_id") + rs.getString("promotion_id"),
                        rs.getString("customer_appointment_id"),
                        promotionTargetExternalRefMapping.get(promotionIdRaw),
                        rs.getInt("promotions_order")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ CustomerAppointmentPromotion done.");
    }
}
