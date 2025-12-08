package de.domschmidt.koku.customer.kafka;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaStreamsStateListener {

    private final StreamsBuilderFactoryBean factoryBean;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostConstruct
    public void registerListener() {
        // KafkaStreams ist erst nach dem Start verfügbar
        factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> {
            kafkaStreams.setStateListener((newState, oldState) -> {
                if (newState == KafkaStreams.State.RUNNING) {
                    applicationEventPublisher.publishEvent(new KafkaStreamsRunningEvent(this));
                }
            });
        });
    }
}