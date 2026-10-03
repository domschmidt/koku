package de.domschmidt.koku.promotion.kafka.promotion.transformer;

import de.domschmidt.koku.promotion.kafka.dto.PromotionKafkaDto;
import de.domschmidt.koku.promotion.persistence.Promotion;
import java.util.ArrayList;

public class PromotionToKafkaPromotionDtoTransformer {

    public PromotionKafkaDto transformToDto(final Promotion model) {
        return PromotionKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .name(model.getName())
                .productManufacturerIds(
                        model.getProductManufacturerIds() != null
                                ? new ArrayList<>(model.getProductManufacturerIds())
                                : null)
                .activityAbsoluteItemSavings(model.getActivityAbsoluteItemSavings())
                .activityAbsoluteSavings(model.getActivityAbsoluteSavings())
                .activityRelativeItemSavings(model.getActivityRelativeItemSavings())
                .activityRelativeSavings(model.getActivityRelativeSavings())
                .productAbsoluteItemSavings(model.getProductAbsoluteItemSavings())
                .productAbsoluteSavings(model.getProductAbsoluteSavings())
                .productRelativeItemSavings(model.getProductRelativeItemSavings())
                .productRelativeSavings(model.getProductRelativeSavings())
                .recorded(model.getRecorded())
                .updated(model.getUpdated())
                .build();
    }
}
