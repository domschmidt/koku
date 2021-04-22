package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.promotion.PromotionActivitySettingsDto;
import de.domschmidt.koku.dto.promotion.PromotionDto;
import de.domschmidt.koku.dto.promotion.PromotionProductSettingsDto;
import de.domschmidt.koku.persistence.model.Promotion;
import de.domschmidt.koku.persistence.model.PromotionActivitySettings;
import de.domschmidt.koku.persistence.model.PromotionProductSettings;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PromotionToPromotionDtoTransformer implements ITransformer<Promotion, PromotionDto> {

    public List<PromotionDto> transformToDtoList(final List<Promotion> modelList) {
        final List<PromotionDto> result = new ArrayList<>();
        for (final Promotion promotion : modelList) {
            result.add(transformToDto(promotion));
        }
        return result;
    }

    public PromotionDto transformToDto(final Promotion model) {
        return PromotionDto.builder()
                .id(model.getId())
                .name(model.getName())
                .startDate(model.getStartDate())
                .endDate(model.getEndDate())
                .productSettings(transformPromoProductSettings(model))
                .activitySettings(transformPromoActivitySettings(model))
                .build();
    }

    private PromotionActivitySettingsDto transformPromoActivitySettings(final Promotion model) {
        PromotionActivitySettings promotionActivitySettings = model.getPromotionActivitySettings();
        if (promotionActivitySettings == null) {
            promotionActivitySettings = new PromotionActivitySettings();
        }
        return PromotionActivitySettingsDto.builder()
                .absoluteItemSavings(promotionActivitySettings.getAbsoluteItemSavings())
                .relativeItemSavings(promotionActivitySettings.getRelativeItemSavings())
                .absoluteSavings(promotionActivitySettings.getAbsoluteSavings())
                .relativeSavings(promotionActivitySettings.getRelativeSavings())
                .build();
    }

    private PromotionProductSettingsDto transformPromoProductSettings(final Promotion model) {
        PromotionProductSettings promotionProductSettings = model.getPromotionProductSettings();
        if (promotionProductSettings == null) {
            promotionProductSettings = new PromotionProductSettings();
        }
        return PromotionProductSettingsDto.builder()
                .absoluteItemSavings(promotionProductSettings.getAbsoluteItemSavings())
                .relativeItemSavings(promotionProductSettings.getRelativeItemSavings())
                .absoluteSavings(promotionProductSettings.getAbsoluteSavings())
                .relativeSavings(promotionProductSettings.getRelativeSavings())
                .build();
    }

    public Promotion transformToEntity(final PromotionDto dtoModel) {
        return Promotion.builder()
                .id(dtoModel.getId())
                .name(dtoModel.getName())
                .startDate(dtoModel.getStartDate())
                .endDate(dtoModel.getEndDate())
                .promotionProductSettings(transformPromoProductSettingsDto(dtoModel))
                .promotionActivitySettings(transformPromoActivitySettingsDto(dtoModel))
                .build();
    }

    private PromotionActivitySettings transformPromoActivitySettingsDto(final PromotionDto dtoModel) {
        PromotionActivitySettingsDto promotionActivitySettings = dtoModel.getActivitySettings();
        if (promotionActivitySettings == null) {
            promotionActivitySettings = new PromotionActivitySettingsDto();
        }
        return PromotionActivitySettings.builder()
                .absoluteItemSavings(promotionActivitySettings.getAbsoluteItemSavings())
                .relativeItemSavings(promotionActivitySettings.getRelativeItemSavings())
                .absoluteSavings(promotionActivitySettings.getAbsoluteSavings())
                .relativeSavings(promotionActivitySettings.getRelativeSavings())
                .build();
    }

    private PromotionProductSettings transformPromoProductSettingsDto(final PromotionDto dtoModel) {
        PromotionProductSettingsDto promotionProductSettings = dtoModel.getProductSettings();
        if (promotionProductSettings == null) {
            promotionProductSettings = new PromotionProductSettingsDto();
        }
        return PromotionProductSettings.builder()
                .absoluteItemSavings(promotionProductSettings.getAbsoluteItemSavings())
                .relativeItemSavings(promotionProductSettings.getRelativeItemSavings())
                .absoluteSavings(promotionProductSettings.getAbsoluteSavings())
                .relativeSavings(promotionProductSettings.getRelativeSavings())
                .build();
    }

    public List<Promotion> transformToEntityList(final List<PromotionDto> promotions) {
        final List<Promotion> result = new ArrayList<>();
        if (promotions != null) {
            for (final PromotionDto promotion : promotions) {
                result.add(transformToEntity(promotion));
            }
        }
        return result;
    }

}
