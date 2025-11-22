package de.domschmidt.koku.promotion.kafka.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@Getter
public class KafkaConfiguration {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapAddress;

}