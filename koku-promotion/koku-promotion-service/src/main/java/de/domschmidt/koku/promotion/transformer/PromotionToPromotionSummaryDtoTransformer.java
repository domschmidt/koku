package de.domschmidt.koku.promotion.transformer;

import de.domschmidt.koku.dto.promotion.KokuPromotionSummaryDto;
import de.domschmidt.koku.promotion.persistence.Promotion;

public class PromotionToPromotionSummaryDtoTransformer {

    public KokuPromotionSummaryDto transformToDto(final Promotion model) {
        return KokuPromotionSummaryDto.builder()
                .id(model.getId())
                .summary(model.getName())
                .build();
    }
}
