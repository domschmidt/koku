package de.domschmidt.koku.kafka.customers.config;

import de.domschmidt.koku.dto.customer.CustomerDto;
import de.domschmidt.koku.kafka.configuration.KafkaConfiguration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaCustomerConfig {

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaCustomerConfig(
            final KafkaConfiguration kafkaConfiguration
    ) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public ProducerFactory<Long, CustomerDto> customerProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                this.kafkaConfiguration.getBootstrapAddress());
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, CustomerDto> customerKafkaTemplate() {
        return new KafkaTemplate<>(customerProducerFactory());
    }
}