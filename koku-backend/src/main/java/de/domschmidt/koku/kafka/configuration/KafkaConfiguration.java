package de.domschmidt.koku.kafka.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Getter
public class KafkaConfiguration {

    @Value("${kafka.bootstrap-server}")
    private String bootstrapAddress;

}