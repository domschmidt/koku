package de.domschmidt.koku;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

import java.util.Locale;

@SpringBootApplication
@EnableConfigServer
public class KokuConfigServiceApplication {

    public static void main(String[] args) {
        Locale.setDefault(Locale.GERMAN);
        SpringApplication.run(KokuConfigServiceApplication.class, args);
    }

}
