package de.domschmidt.koku.customer.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.customer.kafka.customers.config.KafkaCustomerAppointmentConfig;
import de.domschmidt.koku.customer.kafka.customers.config.KafkaCustomerConfig;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKafkaService;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerKafkaService;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerKafkaDto;
import de.domschmidt.koku.customer.kafka.streams.config.KafkaConfiguration;
import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.config.KafkaStreamsCustomizer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaInfrastructureTest {

    @Test
    @SuppressWarnings("unchecked")
    void producerConfigurationsAndServicesPreserveBusinessIdentity() throws Exception {
        final KafkaConfiguration configuration = mock(KafkaConfiguration.class);
        when(configuration.getBootstrapServers()).thenReturn("broker:9092");
        final KafkaCustomerConfig customerConfig = new KafkaCustomerConfig(configuration);
        final KafkaCustomerAppointmentConfig appointmentConfig = new KafkaCustomerAppointmentConfig(configuration);
        assertThat(((DefaultKafkaProducerFactory<?, ?>) customerConfig.customerProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(((DefaultKafkaProducerFactory<?, ?>) appointmentConfig.customerAppointmentProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(customerConfig.customerKafkaTemplate(customerConfig.customerProducerFactory()))
                .isNotNull();
        assertThat(appointmentConfig.customerAppointmentKafkaTemplate(
                        appointmentConfig.customerAppointmentProducerFactory()))
                .isNotNull();

        final KafkaTemplate<Long, CustomerKafkaDto> customerTemplate = mock(KafkaTemplate.class);
        final KafkaTemplate<Long, CustomerAppointmentKafkaDto> appointmentTemplate = mock(KafkaTemplate.class);
        final SendResult<Long, CustomerKafkaDto> customerResult = mock(SendResult.class);
        final SendResult<Long, CustomerAppointmentKafkaDto> appointmentResult = mock(SendResult.class);
        when(customerTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(customerResult));
        when(appointmentTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(appointmentResult));
        final Customer customer = new Customer();
        customer.setId(51L);
        customer.setFirstname("Ada");
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setId(52L);
        appointment.setCustomer(customer);
        appointment.setUserId("u-1");
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 13, 9, 0));
        appointment.setActivities(List.of());
        appointment.setPromotions(List.of());
        appointment.setSoldProducts(List.of());

        assertThat(new CustomerKafkaService(customerTemplate).sendCustomer(customer))
                .isSameAs(customerResult);
        assertThat(new CustomerAppointmentKafkaService(appointmentTemplate).sendCustomerAppointment(appointment))
                .isSameAs(appointmentResult);
        final ArgumentCaptor<CustomerKafkaDto> customerDto = ArgumentCaptor.forClass(CustomerKafkaDto.class);
        final ArgumentCaptor<CustomerAppointmentKafkaDto> appointmentDto =
                ArgumentCaptor.forClass(CustomerAppointmentKafkaDto.class);
        verify(customerTemplate).send(eq(CustomerKafkaDto.TOPIC), eq(51L), customerDto.capture());
        verify(appointmentTemplate).send(eq(CustomerAppointmentKafkaDto.TOPIC), eq(52L), appointmentDto.capture());
        assertThat(customerDto.getValue().getFirstname()).isEqualTo("Ada");
        assertThat(appointmentDto.getValue().getCustomerId()).isEqualTo(51L);
    }

    @Test
    void healthReportsInitializationRebalancingAndRunning() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final KafkaStreamsHealthIndicator indicator = new KafkaStreamsHealthIndicator(factory);
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getDetails()).containsEntry("kafkaStreamsState", "not-initialized");

        final KafkaStreams streams = mock(KafkaStreams.class);
        when(factory.getKafkaStreams()).thenReturn(streams);
        when(streams.state()).thenReturn(KafkaStreams.State.REBALANCING, KafkaStreams.State.RUNNING);
        assertThat(indicator.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(indicator.health().getStatus()).isEqualTo(Status.UP);
    }

    @Test
    void stateListenerPublishesOnlyWhenStreamsBecomeRunning() {
        final StreamsBuilderFactoryBean factory = mock(StreamsBuilderFactoryBean.class);
        final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        new KafkaStreamsStateListener(factory, publisher).registerListener();
        final ArgumentCaptor<KafkaStreamsCustomizer> customizer = ArgumentCaptor.forClass(KafkaStreamsCustomizer.class);
        verify(factory).setKafkaStreamsCustomizer(customizer.capture());
        final KafkaStreams streams = mock(KafkaStreams.class);
        customizer.getValue().customize(streams);
        final ArgumentCaptor<KafkaStreams.StateListener> listener =
                ArgumentCaptor.forClass(KafkaStreams.StateListener.class);
        verify(streams).setStateListener(listener.capture());

        listener.getValue().onChange(KafkaStreams.State.REBALANCING, KafkaStreams.State.CREATED);
        listener.getValue().onChange(KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING);

        verify(publisher, times(1)).publishEvent(any(KafkaStreamsRunningEvent.class));
    }
}
