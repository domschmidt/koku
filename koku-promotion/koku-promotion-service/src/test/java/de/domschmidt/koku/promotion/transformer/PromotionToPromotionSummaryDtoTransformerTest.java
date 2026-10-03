package de.domschmidt.koku.promotion.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.domschmidt.koku.dto.promotion.KokuPromotionSummaryDto;
import de.domschmidt.koku.promotion.persistence.Promotion;
import org.junit.jupiter.api.Test;

class PromotionToPromotionSummaryDtoTransformerTest {

    private final PromotionToPromotionSummaryDtoTransformer transformer =
            new PromotionToPromotionSummaryDtoTransformer();

    @Test
    void summaryIsThePlainNameWithoutManufacturers() {
        final Promotion promotion = new Promotion();
        promotion.setId(7L);
        promotion.setName("Summer");

        final KokuPromotionSummaryDto dto = transformer.transformToDto(promotion);

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getSummary()).isEqualTo("Summer");
    }

    @Test
    void summaryListsProductManufacturers() {
        final Promotion promotion = new Promotion();
        promotion.setName("Summer");

        assertThat(transformer.transformToDto(promotion, "Maker A, Maker B").getSummary())
                .isEqualTo("Summer (Maker A, Maker B)");
    }

    @Test
    void blankManufacturerNamesAreIgnored() {
        final Promotion promotion = new Promotion();
        promotion.setName("Summer");

        assertThat(transformer.transformToDto(promotion, "   ").getSummary()).isEqualTo("Summer");
    }
}
