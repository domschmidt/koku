package de.domschmidt.koku.customer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.customer.kafka.customers.service.CustomerAppointmentKafkaService;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentRepository;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import de.domschmidt.koku.dto.customer.KokuActivityPriceSummaryRequestDto;
import de.domschmidt.koku.dto.customer.KokuActivitySoldProductSummaryRequestDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentOverallPriceSummaryRequestDto;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CustomerAppointmentCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final CustomerAppointmentRepository repository = mock(CustomerAppointmentRepository.class);
    private final CustomerAppointmentKafkaService kafkaService = mock(CustomerAppointmentKafkaService.class);
    private final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer =
            mock(CustomerAppointmentToCustomerAppointmentDtoTransformer.class);
    private CustomerAppointmentController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerAppointmentController(
                entityManager, repository, kafkaService, transformer, null, null, null, null, null, null);
    }

    @Test
    void readAndSummaryTransformExistingAppointmentAndRejectMissingOne() {
        final CustomerAppointment appointment = appointment(5L, 2L, false);
        final KokuCustomerAppointmentDto dto =
                KokuCustomerAppointmentDto.builder().id(5L).build();
        when(repository.findById(5L)).thenReturn(Optional.of(appointment));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(appointment)).thenReturn(dto);

        assertThat(controller.readAppointment(5L)).isSameAs(dto);
        controller.readAppointmentSummary(5L);
        verify(transformer).transformToSummaryDto(appointment);
        assertThatThrownBy(() -> controller.readAppointment(6L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> controller.readAppointmentSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateTransformsFlushesPublishesAndReturnsDto() throws Exception {
        final CustomerAppointment appointment = appointment(5L, 2L, false);
        final KokuCustomerAppointmentDto update =
                KokuCustomerAppointmentDto.builder().version(2L).build();
        final KokuCustomerAppointmentDto result =
                KokuCustomerAppointmentDto.builder().id(5L).build();
        when(entityManager.getReference(CustomerAppointment.class, 5L)).thenReturn(appointment);
        when(transformer.transformToDto(appointment)).thenReturn(result);

        assertThat(controller.update(5L, false, update)).isSameAs(result);
        verify(transformer).transformToEntity(appointment, update);
        verify(entityManager).flush();
        verify(kafkaService).sendCustomerAppointment(appointment);
    }

    @Test
    void updateRejectsStaleVersionUnlessForced() throws Exception {
        final CustomerAppointment appointment = appointment(5L, 3L, false);
        final KokuCustomerAppointmentDto update =
                KokuCustomerAppointmentDto.builder().version(2L).build();
        when(entityManager.getReference(CustomerAppointment.class, 5L)).thenReturn(appointment);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());

        controller.update(5L, true, update);
        verify(transformer).transformToEntity(appointment, update);
    }

    @Test
    void deleteAndRestoreToggleLifecycleAndPublish() throws Exception {
        final CustomerAppointment appointment = appointment(5L, 2L, false);
        when(entityManager.getReference(CustomerAppointment.class, 5L)).thenReturn(appointment);

        controller.delete(5L);
        assertThat(appointment.isDeleted()).isTrue();
        controller.restore(5L);
        assertThat(appointment.isDeleted()).isFalse();
        verify(kafkaService, times(2)).sendCustomerAppointment(appointment);
    }

    @Test
    void lifecycleRejectsRepeatedOperations() {
        final CustomerAppointment appointment = appointment(5L, 2L, true);
        when(entityManager.getReference(CustomerAppointment.class, 5L)).thenReturn(appointment);

        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        appointment.setDeleted(false);
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsPublishesAndTransforms() throws Exception {
        final KokuCustomerAppointmentDto input =
                KokuCustomerAppointmentDto.builder().description("Visit").build();
        final CustomerAppointment entity = appointment(5L, 0L, false);
        final KokuCustomerAppointmentDto result =
                KokuCustomerAppointmentDto.builder().id(5L).build();
        when(transformer.transformToEntity(any(CustomerAppointment.class), org.mockito.Mockito.same(input)))
                .thenReturn(entity);
        when(repository.saveAndFlush(entity)).thenReturn(entity);
        when(transformer.transformToDto(entity)).thenReturn(result);

        assertThat(controller.create(input)).isSameAs(result);
        verify(kafkaService).sendCustomerAppointment(entity);
    }

    @Test
    void kafkaFailureIsExposedAsServerError() throws Exception {
        final CustomerAppointment appointment = appointment(5L, 2L, false);
        when(kafkaService.sendCustomerAppointment(appointment))
                .thenThrow(new ExecutionException(new IllegalStateException("broker")));

        assertThatThrownBy(() -> controller.sendCustomerAppointmentUpdate(appointment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void summaryEndpointsDelegateTheirTypedRequests() {
        final KokuActivityPriceSummaryRequestDto activity = mock(KokuActivityPriceSummaryRequestDto.class);
        final KokuCustomerAppointmentOverallPriceSummaryRequestDto overall =
                mock(KokuCustomerAppointmentOverallPriceSummaryRequestDto.class);
        final KokuActivitySoldProductSummaryRequestDto product = mock(KokuActivitySoldProductSummaryRequestDto.class);

        controller.getActivitySum(activity);
        controller.getActivitySum(overall);
        controller.getProductSum(product);

        verify(transformer).transformToActivityPriceSummary(activity);
        verify(transformer).transformToOverallPriceSummary(overall);
        verify(transformer).transformToSoldProductPriceSummary(product);
    }

    @Test
    void kafkaInterruptRestoresThreadStatusAndReturnsServerError() throws Exception {
        final CustomerAppointment appointment = appointment(5L, 2L, false);
        when(kafkaService.sendCustomerAppointment(appointment)).thenThrow(new InterruptedException("stopped"));

        assertThatThrownBy(() -> controller.sendCustomerAppointmentUpdate(appointment))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(Thread.interrupted()).isTrue();
    }

    private static CustomerAppointment appointment(Long id, Long version, boolean deleted) {
        final CustomerAppointment appointment = new CustomerAppointment();
        appointment.setId(id);
        appointment.setVersion(version);
        appointment.setDeleted(deleted);
        appointment.setStart(LocalDateTime.of(2026, java.time.Month.JULY, 12, 9, 0));
        return appointment;
    }
}
