package exec;

import java.sql.Connection;

public class ProductManufacturerMigration extends BaseMigration {

    public ProductManufacturerMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() {
        logInfo("Migrating ProductManufacturer...");
        final String upsertSql = """
                        INSERT INTO koku.product_manufacturer (external_ref, recorded, updated, deleted, name)
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?)
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      name = EXCLUDED.name,
                                      version = product_manufacturer.version + 1
                        WHERE EXCLUDED.updated > product_manufacturer.updated;
                        """;

        read("SELECT id, recorded, updated, deleted, name FROM koku.product_manufacturer", rs -> {
            try {
                exec(
                        upsertSql,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBoolean("deleted"),
                        rs.getString("name"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        logInfo("ProductManufacturer done.");
    }
}
