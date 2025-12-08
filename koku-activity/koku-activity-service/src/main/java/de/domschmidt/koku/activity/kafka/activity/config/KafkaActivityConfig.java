package de.domschmidt.koku.activity.kafka.activity.config;

import de.domschmidt.koku.activity.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaActivityConfig {

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaActivityConfig(
            final KafkaConfiguration kafkaConfiguration
    ) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public ProducerFactory<Long, ActivityKafkaDto> activityProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                this.kafkaConfiguration.getBootstrapAddress());
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, ActivityKafkaDto> activityKafkaTemplate() {
        return new KafkaTemplate<>(activityProducerFactory());
    }
}