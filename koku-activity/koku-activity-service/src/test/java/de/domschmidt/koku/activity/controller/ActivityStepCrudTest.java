package de.domschmidt.koku.activity.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.activity.kafka.activity.service.ActivityStepKafkaService;
import de.domschmidt.koku.activity.persistence.ActivityStep;
import de.domschmidt.koku.activity.persistence.ActivityStepRepository;
import de.domschmidt.koku.activity.transformer.ActivityStepToActivityStepDtoTransformer;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.activity.KokuActivityStepDto;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ActivityStepCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final ActivityStepRepository repository = mock(ActivityStepRepository.class);
    private final ActivityStepKafkaService kafkaService = mock(ActivityStepKafkaService.class);
    private final ActivityStepToActivityStepDtoTransformer transformer =
            mock(ActivityStepToActivityStepDtoTransformer.class);
    private ActivityStepController controller;

    @BeforeEach
    void setUp() {
        controller = new ActivityStepController(entityManager, repository, kafkaService, transformer);
    }

    @Test
    void readSummaryAndMissingPathsAreDefined() {
        final ActivityStep step = step(false, 2L);
        when(repository.findById(5L)).thenReturn(Optional.of(step));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(step))
                .thenReturn(KokuActivityStepDto.builder().id(5L).build());

        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void staleUpdateRequiresForce() throws Exception {
        final ActivityStep step = step(false, 3L);
        final KokuActivityStepDto update =
                KokuActivityStepDto.builder().version(2L).build();
        when(entityManager.getReference(ActivityStep.class, 5L)).thenReturn(step);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(step, update);
        verify(kafkaService).sendActivityStep(step);
    }

    @Test
    void lifecycleCreateAndKafkaFailurePathsAreDefined() throws Exception {
        final ActivityStep step = step(false, 0L);
        when(entityManager.getReference(ActivityStep.class, 5L)).thenReturn(step);
        controller.delete(5L);
        assertThat(step.isDeleted()).isTrue();
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThat(step.isDeleted()).isFalse();
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);

        final KokuActivityStepDto input =
                KokuActivityStepDto.builder().name("Wash").build();
        when(transformer.transformToEntity(any(ActivityStep.class), org.mockito.Mockito.same(input)))
                .thenReturn(step);
        when(repository.saveAndFlush(step)).thenReturn(step);
        controller.create(input);
        verify(kafkaService, org.mockito.Mockito.atLeastOnce()).sendActivityStep(step);

        when(kafkaService.sendActivityStep(step)).thenThrow(new ExecutionException(new IllegalStateException()));
        assertThatThrownBy(() -> controller.sendActivityStepUpdate(step)).isInstanceOf(ResponseStatusException.class);
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendActivityStep(step);
        assertThatThrownBy(() -> controller.sendActivityStepUpdate(step)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static ActivityStep step(boolean deleted, Long version) {
        final ActivityStep step = new ActivityStep(5L);
        step.setName("Wash");
        step.setVersion(version);
        step.setDeleted(deleted);
        return step;
    }
}
