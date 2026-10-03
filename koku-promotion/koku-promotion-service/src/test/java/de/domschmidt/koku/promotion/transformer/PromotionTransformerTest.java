package de.domschmidt.koku.promotion.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.product.kafka.dto.ProductManufacturerKafkaDto;
import de.domschmidt.koku.promotion.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.promotion.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.promotion.kafka.promotion.transformer.PromotionToKafkaPromotionDtoTransformer;
import de.domschmidt.koku.promotion.persistence.Promotion;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.Test;

class PromotionTransformerTest {

    private static ProductManufacturerKTableProcessor manufacturerProcessor(
            final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> store) {
        final ProductManufacturerKTableProcessor processor = mock(ProductManufacturerKTableProcessor.class);
        when(processor.getProductManufacturers()).thenReturn(store);
        return processor;
    }

    @Test
    void fullUpdateAndRoundTripPreserveEveryDiscountLevel() throws Exception {
        final BigDecimal value = new BigDecimal("12.50");
        final KokuPromotionDto update = KokuPromotionDto.builder()
                .name("Summer")
                .activityAbsoluteItemSavings(value)
                .activityAbsoluteSavings(value)
                .activityRelativeItemSavings(value)
                .activityRelativeSavings(value)
                .productAbsoluteItemSavings(value)
                .productAbsoluteSavings(value)
                .productRelativeItemSavings(value)
                .productRelativeSavings(value)
                .deleted(true)
                .build();
        final Promotion promotion = new Promotion();
        final PromotionToPromotionDtoTransformer transformer =
                new PromotionToPromotionDtoTransformer(manufacturerProcessor(null));

        transformer.transformToEntity(promotion, update);
        final KokuPromotionDto result = transformer.transformToDto(promotion);

        assertThat(result.getName()).isEqualTo("Summer");
        assertThat(result.getActivityAbsoluteItemSavings()).isEqualByComparingTo(value);
        assertThat(result.getActivityAbsoluteSavings()).isEqualByComparingTo(value);
        assertThat(result.getActivityRelativeItemSavings()).isEqualByComparingTo(value);
        assertThat(result.getActivityRelativeSavings()).isEqualByComparingTo(value);
        assertThat(result.getProductAbsoluteItemSavings()).isEqualByComparingTo(value);
        assertThat(result.getProductAbsoluteSavings()).isEqualByComparingTo(value);
        assertThat(result.getProductRelativeItemSavings()).isEqualByComparingTo(value);
        assertThat(result.getProductRelativeSavings()).isEqualByComparingTo(value);
        assertThat(result.getDeleted()).isTrue();
    }

    @Test
    void absentFieldsPreserveExistingPromotion() throws Exception {
        final Promotion promotion = new Promotion();
        promotion.setName("Existing");
        promotion.setActivityAbsoluteSavings(BigDecimal.ONE);

        new PromotionToPromotionDtoTransformer(manufacturerProcessor(null))
                .transformToEntity(promotion, KokuPromotionDto.builder().build());

        assertThat(promotion.getName()).isEqualTo("Existing");
        assertThat(promotion.getActivityAbsoluteSavings()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void productManufacturerIdsAreValidatedAndRoundTripped() throws Exception {
        @SuppressWarnings("unchecked")
        final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> store = mock(ReadOnlyKeyValueStore.class);
        when(store.get(7L))
                .thenReturn(ProductManufacturerKafkaDto.builder()
                        .id(7L)
                        .name("Maker")
                        .build());
        final PromotionToPromotionDtoTransformer transformer =
                new PromotionToPromotionDtoTransformer(manufacturerProcessor(store));
        final Promotion promotion = new Promotion();

        transformer.transformToEntity(
                promotion,
                KokuPromotionDto.builder().productManufacturerIds(List.of(7L)).build());

        assertThat(promotion.getProductManufacturerIds()).containsExactly(7L);
        assertThat(transformer.transformToDto(promotion).getProductManufacturerIds())
                .containsExactly(7L);
    }

    @Test
    void unknownManufacturerIsRejected() {
        @SuppressWarnings("unchecked")
        final ReadOnlyKeyValueStore<Long, ProductManufacturerKafkaDto> store = mock(ReadOnlyKeyValueStore.class);
        final PromotionToPromotionDtoTransformer transformer =
                new PromotionToPromotionDtoTransformer(manufacturerProcessor(store));

        assertThatThrownBy(() -> transformer.transformToEntity(
                        new Promotion(),
                        KokuPromotionDto.builder()
                                .productManufacturerIds(List.of(99L))
                                .build()))
                .isInstanceOf(ManufacturerIdNotFoundException.class);
    }

    @Test
    void kafkaSnapshotContainsEveryDiscountLevelAndProductManufacturers() throws Exception {
        final Promotion promotion = new Promotion();
        promotion.setId(7L);
        promotion.setName("Summer");
        promotion.setDeleted(true);
        promotion.setProductManufacturerIds(new LinkedHashSet<>(List.of(3L, 4L)));
        promotion.setActivityAbsoluteItemSavings(BigDecimal.ONE);
        promotion.setActivityAbsoluteSavings(BigDecimal.valueOf(2));
        promotion.setActivityRelativeItemSavings(BigDecimal.TEN);
        promotion.setActivityRelativeSavings(BigDecimal.valueOf(20));
        promotion.setProductAbsoluteItemSavings(BigDecimal.valueOf(3));
        promotion.setProductAbsoluteSavings(BigDecimal.valueOf(4));
        promotion.setProductRelativeItemSavings(BigDecimal.valueOf(5));
        promotion.setProductRelativeSavings(BigDecimal.valueOf(6));

        final var snapshot = new PromotionToKafkaPromotionDtoTransformer().transformToDto(promotion);

        assertThat(snapshot.getId()).isEqualTo(7L);
        assertThat(snapshot.getName()).isEqualTo("Summer");
        assertThat(snapshot.getDeleted()).isTrue();
        assertThat(snapshot.getProductManufacturerIds()).containsExactlyInAnyOrder(3L, 4L);
        assertThat(snapshot.getActivityAbsoluteSavings()).isEqualByComparingTo("2");
        assertThat(snapshot.getProductRelativeSavings()).isEqualByComparingTo("6");
    }
}
