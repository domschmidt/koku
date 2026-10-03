package de.domschmidt.koku.promotion.transformer;

import de.domschmidt.koku.dto.promotion.KokuPromotionDto;
import de.domschmidt.koku.kafka.productmanufacturers.service.ProductManufacturerKTableProcessor;
import de.domschmidt.koku.promotion.exceptions.ManufacturerIdNotFoundException;
import de.domschmidt.koku.promotion.persistence.Promotion;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PromotionToPromotionDtoTransformer {

    private final ProductManufacturerKTableProcessor productManufacturerKTableProcessor;

    public PromotionToPromotionDtoTransformer(
            final ProductManufacturerKTableProcessor productManufacturerKTableProcessor) {
        this.productManufacturerKTableProcessor = productManufacturerKTableProcessor;
    }

    public KokuPromotionDto transformToDto(final Promotion model) {
        return KokuPromotionDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .productManufacturerIds(
                        model.getProductManufacturerIds() != null
                                ? new ArrayList<>(model.getProductManufacturerIds())
                                : new ArrayList<>())
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

    public Promotion transformToEntity(final Promotion model, final KokuPromotionDto updatedDto)
            throws ManufacturerIdNotFoundException {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        if (updatedDto.getProductManufacturerIds() != null) {
            model.setProductManufacturerIds(resolveProductManufacturerIds(updatedDto.getProductManufacturerIds()));
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
        if (updatedDto.getDeleted() != null) {
            model.setDeleted(updatedDto.getDeleted());
        }

        return model;
    }

    private Set<Long> resolveProductManufacturerIds(final List<Long> productManufacturerIds)
            throws ManufacturerIdNotFoundException {
        final Set<Long> result = new LinkedHashSet<>();
        for (final Long manufacturerId : productManufacturerIds) {
            if (manufacturerId == null) {
                continue;
            }
            if (this.productManufacturerKTableProcessor
                            .getProductManufacturers()
                            .get(manufacturerId)
                    == null) {
                throw new ManufacturerIdNotFoundException(manufacturerId);
            }
            result.add(manufacturerId);
        }
        return result;
    }
}
