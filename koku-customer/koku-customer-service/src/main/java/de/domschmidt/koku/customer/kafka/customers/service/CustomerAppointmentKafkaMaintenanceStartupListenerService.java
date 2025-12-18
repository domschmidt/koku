package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.kafka.KafkaStreamsRunningEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnBooleanProperty("koku.maintenance")
@RequiredArgsConstructor
public class CustomerAppointmentKafkaMaintenanceStartupListenerService implements ApplicationListener<KafkaStreamsRunningEvent> {

    final CustomerAppointmentKafkaMaintenanceService executor;

    @Override
    public void onApplicationEvent(KafkaStreamsRunningEvent event) {
        CompletableFuture
                .delayedExecutor(5, TimeUnit.MINUTES)
                .execute(executor::runMaintenance);
    }
}
