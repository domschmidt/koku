import exec.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class MigrateRunner {

    public static void main(String[] args) throws Exception {

        try (
                Connection source = DriverManager.getConnection(System.getenv("db.source"));

                Connection products = DriverManager.getConnection(System.getenv("db.target.products"));
                Connection promotions = DriverManager.getConnection(System.getenv("db.target.promotions"));
                Connection activities = DriverManager.getConnection(System.getenv("db.target.activities"));
                Connection users = DriverManager.getConnection(System.getenv("db.target.users"));
                Connection customers = DriverManager.getConnection(System.getenv("db.target.customers"));
                Connection files = DriverManager.getConnection(System.getenv("db.target.files"));
        ) {
            String uploadsDir = System.getenv("dir.source.uploads");

            Map<String, String> userMapping = new HashMap<>();
            for (String currentUserMapping : System.getenv("usermapping").split(",")) {
                String[] currentUserMappingSplitted = currentUserMapping.split(":");
                userMapping.put(currentUserMappingSplitted[0], currentUserMappingSplitted[1]);
            }

            source.setAutoCommit(false);
            System.out.println("Starting migration...");

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

            System.out.println("\n🎉 Migration finished successfully.");
        }
    }

}
