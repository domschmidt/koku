import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

public class MigrateRunnerV2 {

    public static void main(String[] args) throws Exception {

        try (
                Connection source = DriverManager.getConnection("jdbc:postgresql://localhost:5433/koku", "admin", "admin");

                Connection products = DriverManager.getConnection("jdbc:postgresql://localhost:5432/products", "postgres", "koku");
                Connection promotions = DriverManager.getConnection("jdbc:postgresql://localhost:5432/promotions", "postgres", "koku");
                Connection activities = DriverManager.getConnection("jdbc:postgresql://localhost:5432/activities", "postgres", "koku");
                Connection users = DriverManager.getConnection("jdbc:postgresql://localhost:5432/users", "postgres", "koku");
                Connection customers = DriverManager.getConnection("jdbc:postgresql://localhost:5432/customers", "postgres", "koku");
                Connection files = DriverManager.getConnection("jdbc:postgresql://localhost:5432/files", "postgres", "koku");
        ) {
            String uploadsDir = "D:/kokubak/2025-11-27_12_00_01/uploads";

            source.setAutoCommit(false);
            Map<String, String> userMapping = Map.of(
                    "852", "d15eac9a-eab4-49a1-b437-d2d6277f82b2",
                    "1", "dbc777d8-e69d-4ffa-b13d-c1686f58599b"
            );

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
