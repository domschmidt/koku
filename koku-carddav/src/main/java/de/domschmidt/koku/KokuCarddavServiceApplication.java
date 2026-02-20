package de.domschmidt.koku;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class KokuCarddavServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuCarddavServiceApplication.class, args);
    }
}
