package de.domschmidt.koku;

import de.domschmidt.koku.activity.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.activity.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.product.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.product.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.promotion.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.user.kafka.users.service.UserKTableProcessor;
import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import({
    ActivityKTableProcessor.class,
    ActivityStepKTableProcessor.class,
    ProductManufacturerKTableProcessor.class,
    ProductKTableProcessor.class,
    PromotionKTableProcessor.class,
    UserKTableProcessor.class
})
public class KokuCustomerServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuCustomerServiceApplication.class, args);
    }
}
