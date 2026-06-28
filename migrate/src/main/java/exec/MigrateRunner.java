package exec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class MigrateRunner {

    private static final Logger LOGGER = Logger.getLogger(MigrateRunner.class.getName());

    public static void main(String[] args) throws SQLException {

        try (Connection source = DriverManager.getConnection(System.getenv("db_source"));
                Connection products = DriverManager.getConnection(System.getenv("db_target_products"));
                Connection promotions = DriverManager.getConnection(System.getenv("db_target_promotions"));
                Connection activities = DriverManager.getConnection(System.getenv("db_target_activities"));
                Connection users = DriverManager.getConnection(System.getenv("db_target_users"));
                Connection customers = DriverManager.getConnection(System.getenv("db_target_customers"));
                Connection files = DriverManager.getConnection(System.getenv("db_target_files")); ) {
            String uploadsDir = System.getenv("dir_source_uploads");

            Map<String, String> userMapping = new HashMap<>();
            for (String currentUserMapping : System.getenv("usermapping").split(",")) {
                String[] currentUserMappingSplitted = currentUserMapping.split(":");
                userMapping.put(currentUserMappingSplitted[0], currentUserMappingSplitted[1]);
            }

            source.setAutoCommit(false);
            LOGGER.info("Starting migration...");

            new ProductManufacturerMigration(source, products).migrate();
            new ProductMigration(source, products).migrate();
            new ProductPriceMigration(source, products).migrate();
            new PromotionMigration(source, promotions).migrate();
            new ActivityStepMigration(source, activities).migrate();
            new ActivityMigration(source, activities).migrate();
            new ActivityPriceMigration(source, activities).migrate();
            new UserMigration(source, users, userMapping).migrate();
            new UserAppointmentMigration(source, users).migrate();
            new CustomerMigration(source, customers).migrate();
            new CustomerAppointmentMigration(source, customers, userMapping).migrate();
            new CustomerAppointmentActivityMigration(source, customers, activities).migrate();
            new CustomerAppointmentActivitySequenceMigration(source, customers, activities, products).migrate();
            new CustomerAppointmentPromotionMigration(source, customers, promotions).migrate();
            new CustomerAppointmentSoldProductMigration(source, customers, products).migrate();
            new FilesMigration(source, files, customers, uploadsDir).migrate();

            LOGGER.info("Migration finished successfully.");
        }
    }
}
