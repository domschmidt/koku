package de.domschmidt.koku.customer.kafka.customers.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.KafkaStreamsRunningEvent;
import de.domschmidt.koku.customer.kafka.streams.config.KafkaConfiguration;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentRepository;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CustomerAppointmentMaintenanceInfrastructureTest {

    @Test
    void scheduledAndStartupListenersDelegateMaintenance() {
        final CustomerAppointmentKafkaMaintenanceService maintenance =
                mock(CustomerAppointmentKafkaMaintenanceService.class);

        new CustomerAppointmentKafkaMaintenanceCronListenerService(maintenance).execute();
        try (var futures = mockStatic(CompletableFuture.class)) {
            final Executor directExecutor = Runnable::run;
            futures.when(() -> CompletableFuture.delayedExecutor(5, TimeUnit.MINUTES))
                    .thenReturn(directExecutor);
            new CustomerAppointmentKafkaMaintenanceStartupListenerService(maintenance)
                    .onApplicationEvent(mock(KafkaStreamsRunningEvent.class));
        }

        verify(maintenance, times(2)).runMaintenance();
    }

    @Test
    void streamConfigurationUsesApplicationAndBrokerProperties() {
        final KafkaConfiguration configuration = new KafkaConfiguration();
        ReflectionTestUtils.setField(configuration, "applicationName", "customer");
        ReflectionTestUtils.setField(configuration, "bootstrapServers", "broker:9092");

        assertThat(configuration.kafkaStreamsConfiguration().asProperties())
                .containsEntry(StreamsConfig.APPLICATION_ID_CONFIG, "customer-streams")
                .containsEntry(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
    }

    @Test
    void appointmentMaintenanceHandlesInterruptedAndFailedPublishes() throws Exception {
        final CustomerAppointmentRepository repository = mock(CustomerAppointmentRepository.class);
        final CustomerAppointmentKafkaService kafkaService = mock(CustomerAppointmentKafkaService.class);
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setActivities(List.of());
        appointment.setPromotions(List.of());
        appointment.setSoldProducts(List.of());
        when(repository.findAll()).thenReturn(List.of(appointment));
        doThrow(new InterruptedException("stopped"), new ExecutionException(new IllegalStateException("broker")))
                .when(kafkaService)
                .sendCustomerAppointment(appointment);
        final CustomerAppointmentKafkaMaintenanceService maintenance = new CustomerAppointmentKafkaMaintenanceService(
                repository, kafkaService, mock(CustomerAppointmentToCustomerAppointmentDtoTransformer.class));

        maintenance.runMaintenance();
        assertThat(Thread.interrupted()).isTrue();
        maintenance.runMaintenance();

        verify(kafkaService, times(2)).sendCustomerAppointment(appointment);
    }
}
