package de.domschmidt.koku.dav.kafka;

import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsHealthIndicator implements HealthIndicator {

    private static final String KAFKA_STREAMS_STATE = "kafkaStreamsState";
    private final StreamsBuilderFactoryBean factoryBean;

    @Autowired
    public KafkaStreamsHealthIndicator(StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @Override
    public Health health() {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            return Health.down()
                    .withDetail(KAFKA_STREAMS_STATE, "not-initialized")
                    .build();
        }
        if (kafkaStreams.state() != KafkaStreams.State.RUNNING) {
            return Health.down()
                    .withDetail(KAFKA_STREAMS_STATE, kafkaStreams.state())
                    .build();
        }
        return Health.up().withDetail(KAFKA_STREAMS_STATE, kafkaStreams.state()).build();
    }
}
