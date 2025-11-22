package de.domschmidt.koku.promotion.transformer;

import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.promotion.persistence.Promotion;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class PromotionToPromotionDtoTransformer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public KokuPromotionDto transformToDto(final Promotion model) {
        return KokuPromotionDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
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

    public Promotion transformToEntity(
            final Promotion model,
            final KokuPromotionDto updatedDto
    ) {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        if (updatedDto.getActivityAbsoluteItemSavings() != null) {
            model.setActivityAbsoluteItemSavings(updatedDto.getActivityAbsoluteItemSavings());
        }
        if (updatedDto.getActivityAbsoluteSavings() != null) {
            model.setActivityAbsoluteSavings(updatedDto.getActivityAbsoluteSavings());
        }
        if (updatedDto.getActivityRelativeItemSavings() != null) {
            model.setActivityRelativeItemSavings(updatedDto.getActivityRelativeItemSavings());
        }
        if (updatedDto.getActivityRelativeSavings() != null) {
            model.setActivityRelativeSavings(updatedDto.getActivityRelativeSavings());
        }
        if (updatedDto.getProductAbsoluteItemSavings() != null) {
            model.setProductAbsoluteItemSavings(updatedDto.getProductAbsoluteItemSavings());
        }
        if (updatedDto.getProductAbsoluteSavings() != null) {
            model.setProductAbsoluteSavings(updatedDto.getProductAbsoluteSavings());
        }
        if (updatedDto.getProductRelativeItemSavings() != null) {
            model.setProductRelativeItemSavings(updatedDto.getProductRelativeItemSavings());
        }
        if (updatedDto.getProductRelativeSavings() != null) {
            model.setProductRelativeSavings(updatedDto.getProductRelativeSavings());
        }

        return model;
    }
}
