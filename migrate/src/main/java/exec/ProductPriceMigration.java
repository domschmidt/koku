package exec;

import java.sql.Connection;

public class ProductPriceMigration extends BaseMigration {

    public ProductPriceMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() {
        logInfo("Migrating ProductPriceHistory...");
        final String upsertSql = """
                        INSERT INTO koku.product_price_history (external_ref, recorded, updated, price, product_id)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, (SELECT ID FROM koku.product where external_ref = ?))
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      price = EXCLUDED.price,
                                      product_id = EXCLUDED.product_id,
                                      version = product_price_history.version + 1
                        WHERE EXCLUDED.updated > product_price_history.updated;
                        """;

        read("SELECT id, recorded, updated, price, product_id FROM koku.product_price_history", rs -> {
            try {
                exec(
                        upsertSql,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBigDecimal("price"),
                        rs.getString("product_id"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        logInfo("ProductPriceHistory done.");
    }
}
