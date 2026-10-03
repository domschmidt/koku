package de.domschmidt.koku.file.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.kafka.streams.config.KafkaConfiguration;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaInfrastructureTest {

    @Test
    void healthIsDownBeforeKafkaStreamsInitialization() {
        final Health health = new KafkaStreamsHealthIndicator(mock(StreamsBuilderFactoryBean.class)).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("kafkaStreamsState", "not-initialized");
    }

    @Test
    void healthReflectsNonRunningKafkaStreams() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.state()).thenReturn(KafkaStreams.State.REBALANCING);

        final Health health = new KafkaStreamsHealthIndicator(factory).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("kafkaStreamsState", KafkaStreams.State.REBALANCING);
    }

    @Test
    void healthIsUpForRunningKafkaStreams() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreams streams = mock(KafkaStreams.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.state()).thenReturn(KafkaStreams.State.RUNNING);

        final Health health = new KafkaStreamsHealthIndicator(factory).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("kafkaStreamsState", KafkaStreams.State.RUNNING);
    }

    @Test
    void kafkaConfigurationUsesApplicationAndBootstrapSettings() {
        final KafkaConfiguration configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationName", "files");
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "broker:9092");

        assertThat(configuration.kafkaStreamsConfiguration().asProperties())
                .containsEntry("application.id", "files-streams")
                .containsEntry("bootstrap.servers", "broker:9092");
    }
}
