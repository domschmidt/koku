import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CustomerAppointmentSoldProductMigration extends BaseMigration {
    private final Connection productTarget;

    public CustomerAppointmentSoldProductMigration(Connection source, Connection target, Connection productTarget) {
        super(source, target);
        this.productTarget = productTarget;
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating CustomerAppointmentSoldProduct...");
        Map<String , Long> productTargetExternalRefMapping = new HashMap<>();
        read("SELECT id, external_ref FROM koku.product", rs -> {
            try {
                productTargetExternalRefMapping.put(rs.getString("external_ref"), rs.getLong("id"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, this.productTarget);

        read("SELECT id, recorded, updated, customer_appointment_id, product_id, position, sell_price FROM koku.customer_appointment_soldproducts_composing", rs -> {
            try {
                String productIdRaw = rs.getString("product_id");
                exec("""
                                INSERT INTO koku.customer_appointment_sold_product (external_ref, recorded, updated, appointment_id, product_id, position, sell_price)
                                VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, (SELECT ID FROM koku.customer_appointment where external_ref = ?), ?, ?, ?)
                                ON CONFLICT (external_ref)
                                DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                              updated = EXCLUDED.updated,
                                              appointment_id = EXCLUDED.appointment_id,
                                              product_id = EXCLUDED.product_id,
                                              position = EXCLUDED.position,
                                              sell_price = EXCLUDED.sell_price,
                                              version = customer_appointment_sold_product.version + 1
                                WHERE EXCLUDED.updated > customer_appointment_sold_product.updated;
                                """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getString("customer_appointment_id"),
                        productTargetExternalRefMapping.get(productIdRaw),
                        rs.getInt("position"),
                        rs.getBigDecimal("sell_price")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ CustomerAppointmentSoldProduct done.");
    }
}
