package de.domschmidt.koku.promotion.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.promotion.kafka.promotion.transformer.PromotionToKafkaPromotionDtoTransformer;
import de.domschmidt.koku.promotion.persistence.Promotion;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PromotionTransformerTest {

    @Test
    void fullUpdateAndRoundTripPreserveEveryDiscountLevel() {
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
        final PromotionToPromotionDtoTransformer transformer = new PromotionToPromotionDtoTransformer();

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
    void absentFieldsPreserveExistingPromotion() {
        final Promotion promotion = new Promotion();
        promotion.setName("Existing");
        promotion.setActivityAbsoluteSavings(BigDecimal.ONE);

        new PromotionToPromotionDtoTransformer()
                .transformToEntity(promotion, KokuPromotionDto.builder().build());

        assertThat(promotion.getName()).isEqualTo("Existing");
        assertThat(promotion.getActivityAbsoluteSavings()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void kafkaSnapshotContainsEveryDiscountLevel() {
        final Promotion promotion = new Promotion();
        promotion.setId(7L);
        promotion.setName("Summer");
        promotion.setDeleted(true);
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
        assertThat(snapshot.getActivityAbsoluteSavings()).isEqualByComparingTo("2");
        assertThat(snapshot.getProductRelativeSavings()).isEqualByComparingTo("6");
    }
}
