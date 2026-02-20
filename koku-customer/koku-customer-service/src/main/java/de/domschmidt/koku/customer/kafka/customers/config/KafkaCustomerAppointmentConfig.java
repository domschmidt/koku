package de.domschmidt.koku.customer.kafka.customers.config;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.streams.config.KafkaConfiguration;
import java.util.Map;
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

@Configuration
@EnableKafka
public class KafkaCustomerAppointmentConfig {

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaCustomerAppointmentConfig(final KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public KafkaTemplate<Long, CustomerAppointmentKafkaDto> customerAppointmentKafkaTemplate(
            ProducerFactory<Long, CustomerAppointmentKafkaDto> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ProducerFactory<Long, CustomerAppointmentKafkaDto> customerAppointmentProducerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfiguration.getBootstrapServers()));
    }
}
