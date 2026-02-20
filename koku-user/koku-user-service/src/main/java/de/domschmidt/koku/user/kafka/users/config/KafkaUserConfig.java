package de.domschmidt.koku.user.kafka.users.config;

import de.domschmidt.koku.user.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
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
public class KafkaUserConfig {

    private final KafkaConfiguration kafkaConfiguration;

    @Autowired
    public KafkaUserConfig(final KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    @Bean
    public ProducerFactory<String, UserKafkaDto> userProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.kafkaConfiguration.getBootstrapAddress());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, UserKafkaDto> userKafkaTemplate() {
        return new KafkaTemplate<>(userProducerFactory());
    }
}
