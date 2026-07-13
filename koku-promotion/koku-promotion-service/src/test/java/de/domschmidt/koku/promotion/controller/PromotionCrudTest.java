package de.domschmidt.koku.promotion.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.business_exception.with_confirmation_message.KokuBusinessExceptionWithConfirmationMessage;
import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.promotion.kafka.promotion.service.PromotionKafkaService;
import de.domschmidt.koku.promotion.persistence.Promotion;
import de.domschmidt.koku.promotion.persistence.PromotionRepository;
import de.domschmidt.koku.promotion.transformer.PromotionToPromotionDtoTransformer;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class PromotionCrudTest {

    private final EntityManager entityManager = mock(EntityManager.class);
    private final PromotionRepository repository = mock(PromotionRepository.class);
    private final PromotionKafkaService kafkaService = mock(PromotionKafkaService.class);
    private final PromotionToPromotionDtoTransformer transformer = mock(PromotionToPromotionDtoTransformer.class);
    private PromotionController controller;

    @BeforeEach
    void setUp() {
        controller = new PromotionController(entityManager, repository, kafkaService, transformer);
    }

    @Test
    void readAndSummaryUsePersistedPromotionAndRejectMissingOne() {
        final Promotion promotion = promotion(5L, 2L, false);
        when(repository.findById(5L)).thenReturn(Optional.of(promotion));
        when(repository.findById(6L)).thenReturn(Optional.empty());
        when(transformer.transformToDto(promotion))
                .thenReturn(KokuPromotionDto.builder().id(5L).build());

        assertThat(controller.read(5L).getId()).isEqualTo(5L);
        assertThat(controller.readSummary(5L).getId()).isEqualTo(5L);
        assertThatThrownBy(() -> controller.read(6L)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.readSummary(6L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateRejectsStaleVersionUnlessForced() throws Exception {
        final Promotion promotion = promotion(5L, 3L, false);
        final KokuPromotionDto update = KokuPromotionDto.builder().version(2L).build();
        when(entityManager.getReference(Promotion.class, 5L)).thenReturn(promotion);

        assertThatThrownBy(() -> controller.update(5L, false, update))
                .isInstanceOf(KokuBusinessExceptionWithConfirmationMessage.class);
        verify(transformer, never()).transformToEntity(any(), any());
        controller.update(5L, true, update);
        verify(transformer).transformToEntity(promotion, update);
        verify(entityManager).flush();
        verify(kafkaService).sendPromotion(promotion);
    }

    @Test
    void deleteAndRestoreToggleLifecycleWhileRepeatedOperationsFail() {
        final Promotion promotion = promotion(5L, 2L, false);
        when(entityManager.getReference(Promotion.class, 5L)).thenReturn(promotion);

        controller.delete(5L);
        assertThat(promotion.isDeleted()).isTrue();
        assertThatThrownBy(() -> controller.delete(5L)).isInstanceOf(ResponseStatusException.class);
        controller.restore(5L);
        assertThat(promotion.isDeleted()).isFalse();
        assertThatThrownBy(() -> controller.restore(5L)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void createPersistsPublishesAndTransforms() throws Exception {
        final KokuPromotionDto input = KokuPromotionDto.builder().name("Summer").build();
        final Promotion promotion = promotion(5L, 0L, false);
        when(transformer.transformToEntity(any(Promotion.class), org.mockito.Mockito.same(input)))
                .thenReturn(promotion);
        when(repository.saveAndFlush(promotion)).thenReturn(promotion);

        controller.create(input);

        verify(kafkaService).sendPromotion(promotion);
        verify(transformer).transformToDto(promotion);
    }

    @Test
    void kafkaFailureIsExposed() throws Exception {
        final Promotion promotion = promotion(5L, 2L, false);
        when(kafkaService.sendPromotion(promotion))
                .thenThrow(new ExecutionException(new IllegalStateException("broker")));

        assertThatThrownBy(() -> controller.sendPromotionUpdate(promotion)).isInstanceOf(ResponseStatusException.class);

        doThrow(new InterruptedException("stopped")).when(kafkaService).sendPromotion(promotion);
        assertThatThrownBy(() -> controller.sendPromotionUpdate(promotion)).isInstanceOf(ResponseStatusException.class);
        assertThat(Thread.interrupted()).isTrue();
    }

    private static Promotion promotion(Long id, Long version, boolean deleted) {
        final Promotion promotion = new Promotion();
        promotion.setId(id);
        promotion.setVersion(version);
        promotion.setName("Summer");
        promotion.setDeleted(deleted);
        return promotion;
    }
}
