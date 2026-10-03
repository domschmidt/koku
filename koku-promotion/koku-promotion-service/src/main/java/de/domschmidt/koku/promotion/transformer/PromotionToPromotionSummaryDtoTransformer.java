package de.domschmidt.koku.promotion.transformer;

import de.domschmidt.koku.dto.promotion.KokuPromotionSummaryDto;
import de.domschmidt.koku.promotion.persistence.Promotion;

public class PromotionToPromotionSummaryDtoTransformer {

    public KokuPromotionSummaryDto transformToDto(final Promotion model) {
        return transformToDto(model, null);
    }

    public KokuPromotionSummaryDto transformToDto(final Promotion model, final String manufacturerNames) {
        final String name = model.getName() != null ? model.getName() : "";
        final String summary = manufacturerNames != null && !manufacturerNames.isBlank()
                ? name + " (" + manufacturerNames + ")"
                : name;
        return KokuPromotionSummaryDto.builder()
                .id(model.getId())
                .summary(summary)
                .build();
    }
}
