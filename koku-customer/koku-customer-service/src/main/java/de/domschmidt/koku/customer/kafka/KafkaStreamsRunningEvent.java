package de.domschmidt.koku.customer.kafka;

import org.springframework.context.ApplicationEvent;

public class KafkaStreamsRunningEvent extends ApplicationEvent {

    public KafkaStreamsRunningEvent(Object source) {
        super(source);
    }
}
