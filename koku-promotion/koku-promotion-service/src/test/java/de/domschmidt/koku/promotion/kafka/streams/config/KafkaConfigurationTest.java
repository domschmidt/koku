package de.domschmidt.koku.promotion.kafka.streams.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConfigurationTest {

    @Test
    void buildsStreamsConfigFromApplicationNameAndBootstrapServers() {
        final KafkaConfiguration configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationName", "koku-promotion-service");
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "broker:9092");

        assertThat(configuration.kafkaStreamsConfiguration().asProperties())
                .containsEntry(StreamsConfig.APPLICATION_ID_CONFIG, "koku-promotion-service-streams")
                .containsEntry(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
    }
}
