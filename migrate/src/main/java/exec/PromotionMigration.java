package exec;

import java.sql.Connection;

public class PromotionMigration extends BaseMigration {

    public PromotionMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating Promotion...");

        read("""
                 SELECT promotion.id, promotion.recorded, promotion.updated, promotion.deleted, promotion.name,
                        actset.absolute_item_savings as actset_absolute_item_savings, actset.absolute_savings as actset_absolute_savings, actset.relative_item_savings as actset_relative_item_savings, actset.relative_savings as actset_relative_savings,
                        prodset.absolute_item_savings as prodset_absolute_item_savings, prodset.absolute_savings as prodset_absolute_savings, prodset.relative_item_savings as prodset_relative_item_savings, prodset.relative_savings as prodset_relative_savings
                 FROM koku.promotion promotion
                 LEFT OUTER JOIN koku.promotion_activity_settings actset ON (actset.id = promotion_activity_settings_id)
                 LEFT OUTER JOIN koku.promotion_product_settings prodset ON (prodset.id = promotion_product_settings_id)
                 """, rs -> {
            try {
                exec("""
                        INSERT INTO koku.promotion (
                          external_ref, recorded, updated, deleted, name, 
                          activity_absolute_item_savings, activity_absolute_savings, activity_relative_item_savings, activity_relative_savings,
                          product_absolute_item_savings, product_absolute_savings, product_relative_item_savings, product_relative_savings
                        )
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?)
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      name = EXCLUDED.name,
                                      activity_absolute_item_savings = EXCLUDED.activity_absolute_item_savings,
                                      activity_absolute_savings = EXCLUDED.activity_absolute_savings,
                                      activity_relative_item_savings = EXCLUDED.activity_relative_item_savings,
                                      activity_relative_savings = EXCLUDED.activity_relative_savings,
                                      product_absolute_item_savings = EXCLUDED.product_absolute_item_savings,
                                      product_absolute_savings = EXCLUDED.product_absolute_savings,
                                      product_relative_item_savings = EXCLUDED.product_relative_item_savings,
                                      product_relative_savings = EXCLUDED.product_relative_savings,
                                      version = promotion.version + 1
                        WHERE EXCLUDED.updated > promotion.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getBoolean("deleted"),
                        rs.getString("name"),
                        rs.getBigDecimal("actset_absolute_item_savings"),
                        rs.getBigDecimal("actset_absolute_savings"),
                        rs.getBigDecimal("actset_relative_item_savings"),
                        rs.getBigDecimal("actset_relative_savings"),
                        rs.getBigDecimal("prodset_absolute_item_savings"),
                        rs.getBigDecimal("prodset_absolute_savings"),
                        rs.getBigDecimal("prodset_relative_item_savings"),
                        rs.getBigDecimal("prodset_relative_savings")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ Promotion done.");
    }
}
