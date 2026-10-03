package de.domschmidt.koku;

import de.domschmidt.koku.product.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import(ProductManufacturerKTableProcessor.class)
public class KokuPromotionServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuPromotionServiceApplication.class, args);
    }
}
