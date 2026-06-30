package de.domschmidt.koku.customer.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaStreamsHealthIndicator implements HealthIndicator {

    private static final String KAFKA_STREAMS_STATE = "kafkaStreamsState";
    private static final String NOT_INITIALIZED_STATE = "not-initialized";

    private final StreamsBuilderFactoryBean factoryBean;

    @Override
    public Health health() {
        final KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            return Health.down()
                    .withDetail(KAFKA_STREAMS_STATE, NOT_INITIALIZED_STATE)
                    .build();
        }
        final KafkaStreams.State kafkaStreamsState = kafkaStreams.state();
        if (kafkaStreamsState != KafkaStreams.State.RUNNING) {
            return Health.down()
                    .withDetail(KAFKA_STREAMS_STATE, kafkaStreamsState)
                    .build();
        }
        return Health.up().withDetail(KAFKA_STREAMS_STATE, kafkaStreamsState).build();
    }
}
