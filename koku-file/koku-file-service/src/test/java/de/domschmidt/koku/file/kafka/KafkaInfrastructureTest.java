package de.domschmidt.koku.file.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.file.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.file.kafka.customers.service.CustomerKTableProcessor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaInfrastructureTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void customerProcessorBuildsAndExposesItsStore() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final StreamsBuilder builder = new StreamsBuilder();
        final KafkaStreams streams = mock(KafkaStreams.class);
        final ReadOnlyKeyValueStore store = mock(ReadOnlyKeyValueStore.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.store(any())).thenReturn(store);

        final CustomerKTableProcessor processor = new CustomerKTableProcessor(factory);

        assertThat(processor.customerKTable(builder)).isNotNull();
        assertThat(builder.build().describe().subtopologies()).isNotEmpty();
        assertThat(processor.getCustomers()).isSameAs(store);
    }

    @Test
    void customerProcessorRejectsAccessBeforeKafkaStreamsStart() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final CustomerKTableProcessor processor = new CustomerKTableProcessor(factory);

        assertThatThrownBy(processor::getCustomers)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Kafka Streams are not started");
    }

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
