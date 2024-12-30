package de.domschmidt.koku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Locale;

@SpringBootApplication
public class KokuGatewayServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuGatewayServiceApplication.class, args);
    }

}
