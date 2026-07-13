package exec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;

public class MigrateRunner {

    private static final Logger LOGGER = Logger.getLogger(MigrateRunner.class.getName());

    public static void main(String[] args) throws SQLException {

        String[] configuration = configuration(args, System::getenv);

        try (Connection source = DriverManager.getConnection(configuration[0]);
                Connection products = DriverManager.getConnection(configuration[1]);
                Connection promotions = DriverManager.getConnection(configuration[2]);
                Connection activities = DriverManager.getConnection(configuration[3]);
                Connection users = DriverManager.getConnection(configuration[4]);
                Connection customers = DriverManager.getConnection(configuration[5]);
                Connection files = DriverManager.getConnection(configuration[6]); ) {
            String uploadsDir = configuration[7];

            Map<String, String> userMapping = new HashMap<>();
            for (String currentUserMapping : configuration[8].split(",")) {
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

    static String[] configuration(final String[] args, final UnaryOperator<String> environment) {
        return args.length == 9
                ? args
                : new String[] {
                    environment.apply("db_source"),
                    environment.apply("db_target_products"),
                    environment.apply("db_target_promotions"),
                    environment.apply("db_target_activities"),
                    environment.apply("db_target_users"),
                    environment.apply("db_target_customers"),
                    environment.apply("db_target_files"),
                    environment.apply("dir_source_uploads"),
                    environment.apply("usermapping")
                };
    }
}
