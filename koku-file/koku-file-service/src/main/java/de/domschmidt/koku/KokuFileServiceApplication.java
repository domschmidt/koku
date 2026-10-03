package de.domschmidt.koku;

import de.domschmidt.koku.customer.kafka.customers.service.CustomerKTableProcessor;
import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import(CustomerKTableProcessor.class)
public class KokuFileServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuFileServiceApplication.class, args);
    }
}
