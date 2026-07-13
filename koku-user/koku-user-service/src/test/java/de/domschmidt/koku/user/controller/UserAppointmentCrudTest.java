package de.domschmidt.koku.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.user.KokuUserAppointmentDto;
import de.domschmidt.koku.user.kafka.users.service.UserAppointmentKafkaService;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserAppointment;
import de.domschmidt.koku.user.persistence.UserAppointmentRepository;
import de.domschmidt.koku.user.transformer.UserAppointmentToUserAppointmentDtoTransformer;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UserAppointmentCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final UserAppointmentRepository repository = mock(UserAppointmentRepository.class);
    private final UserAppointmentToUserAppointmentDtoTransformer transformer =
            mock(UserAppointmentToUserAppointmentDtoTransformer.class);
    private final UserAppointmentKafkaService kafkaService = mock(UserAppointmentKafkaService.class);
    private UserAppointmentController controller;

    @BeforeEach
    void setUp() {
        controller = new UserAppointmentController(entityManager, repository, transformer, kafkaService);
    }

    @Test
    void readTransformsExistingAppointmentAndRejectsMissingOne() {
        final UserAppointment appointment = appointment(5L, 2L, false);
        final KokuUserAppointmentDto dto =
                KokuUserAppointmentDto.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(appointment));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(appointment)).thenReturn(dto);

        assertThat(controller.readAppointment(5L)).isSameAs(dto);
        assertThatThrownBy(() -> controller.readAppointment(6L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void summaryContainsAppointmentDate() {
        final UserAppointment appointment = appointment(5L, 2L, false);
        appointment.setStartTimestamp(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0));
        when(repository.findById(5L)).thenReturn(Optional.of(appointment));

        assertThat(controller.readAppointmentSummary(5L).getSummary()).isEqualTo("Privater Termin vom 12.07.2026");
        assertThatThrownBy(() -> controller.readAppointmentSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateTransformsFlushesPublishesAndReturnsDto() throws Exception {
        final UserAppointment appointment = appointment(5L, 2L, false);
        final KokuUserAppointmentDto update =
                KokuUserAppointmentDto.builder().version(2L).build();
        final KokuUserAppointmentDto result =
                KokuUserAppointmentDto.builder().id(5L).build();
        when(entityManager.getReference(UserAppointment.class, 5L)).thenReturn(appointment);
        when(transformer.transformToDto(appointment)).thenReturn(result);

        assertThat(controller.update(5L, false, update)).isSameAs(result);
        verify(transformer).transformToEntity(appointment, update);
        verify(entityManager).flush();
        verify(kafkaService).sendUserAppointment(appointment);
    }

    @Test
    void updateRejectsStaleVersionUnlessForced() {
        final UserAppointment appointment = appointment(5L, 3L, false);
        final KokuUserAppointmentDto update =
                KokuUserAppointmentDto.builder().version(2L).build();
        when(entityManager.getReference(UserAppointment.class, 5L)).thenReturn(appointment);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());

        controller.update(5L, true, update);
        verify(transformer).transformToEntity(appointment, update);
    }

    @Test
    void deleteAndRestoreToggleLifecycle() {
        final UserAppointment appointment = appointment(5L, 2L, false);
        when(entityManager.getReference(UserAppointment.class, 5L)).thenReturn(appointment);

        controller.delete(5L);
        assertThat(appointment.isDeleted()).isTrue();
        controller.restore(5L);
        assertThat(appointment.isDeleted()).isFalse();
    }

    @Test
    void lifecycleRejectsRepeatedOperations() {
        final UserAppointment appointment = appointment(5L, 2L, true);
        when(entityManager.getReference(UserAppointment.class, 5L)).thenReturn(appointment);

        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        appointment.setDeleted(false);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsPublishesAndReturnsTransformedAppointment() throws Exception {
        final KokuUserAppointmentDto input =
                KokuUserAppointmentDto.builder().description("Focus").build();
        final UserAppointment entity = appointment(5L, 0L, false);
        final KokuUserAppointmentDto result =
                KokuUserAppointmentDto.builder().id(5L).build();
        when(transformer.transformToEntity(any(UserAppointment.class), org.mockito.Mockito.same(input)))
                .thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(transformer.transformToDto(entity)).thenReturn(result);

        assertThat(controller.create(input)).isSameAs(result);
        verify(kafkaService).sendUserAppointment(entity);
    }

    @Test
    void kafkaExecutionFailureDoesNotFailAppointmentMutation() throws Exception {
        final UserAppointment appointment = appointment(5L, 2L, false);
        when(kafkaService.sendUserAppointment(appointment))
                .thenThrow(new ExecutionException(new IllegalStateException("broker")));

        controller.sendUserAppointmentUpdate(appointment);

        verify(kafkaService).sendUserAppointment(appointment);
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendUserAppointment(appointment);
        controller.sendUserAppointmentUpdate(appointment);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static UserAppointment appointment(Long id, Long version, boolean deleted) {
        final UserAppointment appointment = new UserAppointment();
        appointment.setId(id);
        appointment.setVersion(version);
        appointment.setDeleted(deleted);
        appointment.setUser(new User("u-1"));
        return appointment;
    }
}
