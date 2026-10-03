package de.domschmidt.koku.dav.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.kafka.streams.config.KafkaConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaConfigurationTest {

    @Test
    void kafkaConfigurationUsesApplicationAndBootstrapSettings() {
        final KafkaConfiguration configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationName", "dav");
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "broker:9092");

        assertThat(configuration.kafkaStreamsConfiguration().asProperties())
                .containsEntry("application.id", "dav-streams")
                .containsEntry("bootstrap.servers", "broker:9092");
    }
}
