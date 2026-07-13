package de.domschmidt.koku.dav.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Status;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

class KafkaStreamsHealthIndicatorTest {

    private final StreamsBuilderFactoryBean factoryBean = mock(StreamsBuilderFactoryBean.class);
    private final KafkaStreamsHealthIndicator indicator = new KafkaStreamsHealthIndicator(factoryBean);

    @Test
    void reportsDownBeforeStreamsAreInitialized() {
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("kafkaStreamsState", "not-initialized");
    }

    @Test
    void reportsDownWhileStreamsAreRebalancing() {
        final KafkaStreams streams = streamsIn(KafkaStreams.State.REBALANCING);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("kafkaStreamsState", KafkaStreams.State.REBALANCING);
        assertThat(streams.state()).isEqualTo(KafkaStreams.State.REBALANCING);
    }

    @Test
    void reportsUpOnlyForRunningStreams() {
        streamsIn(KafkaStreams.State.RUNNING);

        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
        assertThat(indicator.health().getDetails()).containsEntry("kafkaStreamsState", KafkaStreams.State.RUNNING);
    }

    private KafkaStreams streamsIn(KafkaStreams.State state) {
        final KafkaStreams streams = mock(KafkaStreams.class);
        when(streams.state()).thenReturn(state);
        when(factoryBean.getKafkaStreams()).thenReturn(streams);
        return streams;
    }
}
