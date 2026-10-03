package de.domschmidt.koku;

import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKTableProcessor;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKTableProcessor;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKTableProcessor;
import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@Import({CustomerAppointmentKTableProcessor.class, CustomerKTableProcessor.class, UserAppointmentKTableProcessor.class})
public class KokuDavServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuDavServiceApplication.class, args);
    }
}
