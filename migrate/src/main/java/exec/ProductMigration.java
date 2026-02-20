package exec;

import java.sql.Connection;

public class ProductMigration extends BaseMigration {

    public ProductMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating Product...");

        read("SELECT id, recorded, updated, deleted, description, manufacturer_id FROM koku.product", rs -> {
            try {
                exec(
                        """
                        INSERT INTO koku.product (external_ref, recorded, updated, deleted, name, manufacturer_id)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?, (SELECT ID FROM koku.product_manufacturer where external_ref = ?))
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      name = EXCLUDED.name,
                                      manufacturer_id = EXCLUDED.manufacturer_id,
                                      version = product.version + 1
                        WHERE EXCLUDED.updated > product.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBoolean("deleted"),
                        rs.getString("description"),
                        rs.getString("manufacturer_id"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ Product done.");
    }
}
