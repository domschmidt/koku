package de.domschmidt.koku.activity.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.chart.dto.response.axis.CategoricalXAxisDto;
import de.domschmidt.koku.activity.kafka.activity.service.ActivityKafkaService;
import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.activity.persistence.ActivityPriceHistoryEntry;
import de.domschmidt.koku.activity.persistence.ActivityRepository;
import de.domschmidt.koku.activity.transformer.ActivityToActivityDtoTransformer;
import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.activity.KokuActivityDto;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class ActivityCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final ActivityRepository repository = mock(ActivityRepository.class);
    private final ActivityKafkaService kafkaService = mock(ActivityKafkaService.class);
    private final ActivityToActivityDtoTransformer transformer = mock(ActivityToActivityDtoTransformer.class);
    private ActivityController controller;

    @BeforeEach
    void setUp() {
        controller = new ActivityController(entityManager, repository, kafkaService, transformer);
    }

    @Test
    void readAndSummaryUsePersistedActivityAndRejectMissingOne() {
        final Activity activity = activity(false, 2L);
        when(repository.findById(5L)).thenReturn(Optional.of(activity));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(activity))
                .thenReturn(KokuActivityDto.builder().id(5L).build());

        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void staleUpdateRequiresForce() throws Exception {
        final Activity activity = activity(false, 3L);
        final KokuActivityDto update = KokuActivityDto.builder().version(2L).build();
        when(entityManager.getReference(Activity.class, 5L)).thenReturn(activity);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(activity, update);
        verify(kafkaService).sendActivity(activity);
    }

    @Test
    void lifecycleTogglesAndRejectsRepeatedOperations() {
        final Activity activity = activity(false, 2L);
        when(entityManager.getReference(Activity.class, 5L)).thenReturn(activity);

        controller.delete(5L);
        assertThat(activity.isDeleted()).isTrue();
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThat(activity.isDeleted()).isFalse();
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsAndPublishes() throws Exception {
        final KokuActivityDto input = KokuActivityDto.builder().name("Cut").build();
        final Activity activity = activity(false, 0L);
        when(transformer.transformToEntity(any(Activity.class), org.mockito.Mockito.same(input)))
                .thenReturn(activity);
        when(repository.saveAndFlush(activity)).thenReturn(activity);

        controller.create(input);

        verify(kafkaService).sendActivity(activity);
        verify(transformer).transformToDto(activity);
    }

    @Test
    void kafkaFailureIsExposed() throws Exception {
        final Activity activity = activity(false, 2L);
        when(kafkaService.sendActivity(activity)).thenThrow(new ExecutionException(new IllegalStateException()));

        assertThatThrownBy(() -> controller.sendActivityUpdate(activity)).isInstanceOf(ResponseStatusException.class);
        doThrow(new InterruptedException("stopped")).when(kafkaService).sendActivity(activity);
        assertThatThrownBy(() -> controller.sendActivityUpdate(activity)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    @Test
    void priceHistoryCreatesChartAndRejectsMissingActivity() {
        final Activity activity = activity(false, 2L);
        final ActivityPriceHistoryEntry first = new ActivityPriceHistoryEntry(activity, new BigDecimal("49.90"));
        first.setRecorded(LocalDateTime.of(2026, java.time.Month.JULY, 14, 10, 30));
        final ActivityPriceHistoryEntry second = new ActivityPriceHistoryEntry(activity, new BigDecimal("59.90"));
        second.setRecorded(LocalDateTime.of(2026, java.time.Month.JULY, 15, 11, 45));
        activity.getPriceHistory().addAll(java.util.List.of(first, second));
        when(repository.findById(5L)).thenReturn(Optional.of(activity));
        when(repository.findById(6L)).thenReturn(Optional.empty());

        final var chart = controller.readPriceHistory(5L);

        assertThat(chart.getSeries())
                .singleElement()
                .extracting("data")
                .asList()
                .containsExactly(new BigDecimal("49.90"), new BigDecimal("59.90"));
        assertThat(((CategoricalXAxisDto) chart.getAxes().getX()).getCategories())
                .containsExactly("14.07.2026 10:30 Uhr", "15.07.2026 11:45 Uhr");
        assertThatThrownBy(() -> controller.readPriceHistory(6L)).isInstanceOf(ResponseStatusException.class);
    }

    private static Activity activity(boolean deleted, Long version) {
        final Activity activity = new Activity();
        activity.setId(5L);
        activity.setName("Cut");
        activity.setVersion(version);
        activity.setDeleted(deleted);
        return activity;
    }
}
