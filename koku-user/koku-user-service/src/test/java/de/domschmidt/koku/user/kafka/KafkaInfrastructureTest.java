package de.domschmidt.koku.user.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.user.kafka.config.KafkaConfiguration;
import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.koku.user.kafka.users.config.KafkaUserConfig;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKafkaMaintenanceService;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKafkaService;
import de.domschmidt.koku.user.kafka.users.service.UserKafkaMaintenanceService;
import de.domschmidt.koku.user.kafka.users.service.UserKafkaService;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserAppointment;
import de.domschmidt.koku.user.persistence.UserAppointmentRepository;
import de.domschmidt.koku.user.persistence.UserRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaInfrastructureTest {

    @Test
    void producerConfigurationsUseConfiguredBrokerAndCreateTemplates() {
        assertThat(new KafkaConfiguration().getBootstrapAddress()).isNull();
        final KafkaConfiguration configuration = mock(KafkaConfiguration.class);
        when(configuration.getBootstrapAddress()).thenReturn("broker:9092");
        final KafkaUserConfig config = new KafkaUserConfig(configuration);

        assertThat(((DefaultKafkaProducerFactory<?, ?>) config.userProducerFactory()).getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(((DefaultKafkaProducerFactory<?, ?>) config.userAppointmentProducerFactory())
                        .getConfigurationProperties())
                .containsEntry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker:9092");
        assertThat(config.userKafkaTemplate()).isNotNull();
        assertThat(config.userAppointmentKafkaTemplate()).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void servicesPublishTransformedEntitiesWithTheirIdentity() throws Exception {
        final KafkaTemplate<String, UserKafkaDto> userTemplate = mock(KafkaTemplate.class);
        final KafkaTemplate<Long, UserAppointmentKafkaDto> appointmentTemplate = mock(KafkaTemplate.class);
        final SendResult<String, UserKafkaDto> userResult = mock(SendResult.class);
        final SendResult<Long, UserAppointmentKafkaDto> appointmentResult = mock(SendResult.class);
        when(userTemplate.send(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(userResult));
        when(appointmentTemplate.send(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(appointmentResult));
        final User user = new User("u-1");
        user.setFirstname("Ada");
        final UserAppointment appointment = new UserAppointment();
        appointment.setId(41L);
        appointment.setUser(user);

        assertThat(new UserKafkaService(userTemplate).sendUser(user)).isSameAs(userResult);
        assertThat(new UserAppointmentKafkaService(appointmentTemplate).sendUserAppointment(appointment))
                .isSameAs(appointmentResult);

        final ArgumentCaptor<UserKafkaDto> userDto = ArgumentCaptor.forClass(UserKafkaDto.class);
        final ArgumentCaptor<UserAppointmentKafkaDto> appointmentDto =
                ArgumentCaptor.forClass(UserAppointmentKafkaDto.class);
        verify(userTemplate).send(eq(UserKafkaDto.TOPIC), eq("u-1"), userDto.capture());
        verify(appointmentTemplate).send(eq(UserAppointmentKafkaDto.TOPIC), eq(41L), appointmentDto.capture());
        assertThat(userDto.getValue().getFirstname()).isEqualTo("Ada");
        assertThat(appointmentDto.getValue().getUserId()).isEqualTo("u-1");
    }

    @Test
    void maintenancePublishesAndHandlesFailures() throws Exception {
        final UserRepository users = mock(UserRepository.class);
        final UserAppointmentRepository appointments = mock(UserAppointmentRepository.class);
        final UserKafkaService userService = mock(UserKafkaService.class);
        final UserAppointmentKafkaService appointmentService = mock(UserAppointmentKafkaService.class);
        final User user = new User("u-1");
        final UserAppointment appointment = new UserAppointment();
        when(users.findAll()).thenReturn(List.of(user));
        when(appointments.findAll()).thenReturn(List.of(appointment));

        new UserKafkaMaintenanceService(users, userService).onApplicationEvent(null);
        new UserAppointmentKafkaMaintenanceService(appointments, appointmentService).onApplicationEvent(null);

        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(userService)
                .sendUser(user);
        doThrow(new InterruptedException("stopped")).when(appointmentService).sendUserAppointment(appointment);
        new UserKafkaMaintenanceService(users, userService).onApplicationEvent(null);
        new UserAppointmentKafkaMaintenanceService(appointments, appointmentService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
        verify(userService, atLeastOnce()).sendUser(user);
        verify(appointmentService, atLeastOnce()).sendUserAppointment(appointment);

        doThrow(new InterruptedException("stopped")).when(userService).sendUser(user);
        doThrow(new ExecutionException(new IllegalStateException("broker")))
                .when(appointmentService)
                .sendUserAppointment(appointment);
        new UserKafkaMaintenanceService(users, userService).onApplicationEvent(null);
        assertThat(Thread.interrupted()).isTrue();
        new UserAppointmentKafkaMaintenanceService(appointments, appointmentService).onApplicationEvent(null);
    }
}
