package de.domschmidt.koku.customer.kafka.customers.config;

import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.customer.kafka.streams.config.KafkaConfiguration;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
public class KafkaCustomerConfig {

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaCustomerConfig(
            final KafkaConfiguration kafkaConfiguration
    ) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public KafkaTemplate<Long, CustomerKafkaDto> customerKafkaTemplate(
            ProducerFactory<Long, CustomerKafkaDto> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<Long, CustomerKafkaDto> customerProducerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfiguration.getBootstrapServers()
        ));
    }

}