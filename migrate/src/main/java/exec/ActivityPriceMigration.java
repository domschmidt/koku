package exec;

import java.sql.Connection;

public class ActivityPriceMigration extends BaseMigration {

    public ActivityPriceMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating ActivityPriceHistory...");

        read("SELECT id, recorded, updated, price, activity_id FROM koku.activity_price_history", rs -> {
            try {
                exec(
                        """
                        INSERT INTO koku.activity_price_history (external_ref, recorded, updated, price, activity_id)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, (SELECT ID FROM koku.activity where external_ref = ?))
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      price = EXCLUDED.price,
                                      activity_id = EXCLUDED.activity_id,
                                      version = activity_price_history.version + 1
                        WHERE EXCLUDED.updated > activity_price_history.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBigDecimal("price"),
                        rs.getString("activity_id"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ ActivityPriceHistory done.");
    }
}
