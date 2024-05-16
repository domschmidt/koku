package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.activity.ActivityDto;
import de.domschmidt.koku.persistence.model.Activity;
import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import de.domschmidt.koku.service.searchoptions.ActivitySearchOptions;
import de.domschmidt.koku.transformer.common.ITransformer;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityToActivityDtoTransformer implements ITransformer<Activity, ActivityDto> {

    public List<ActivityDto> transformToDtoList(final List<Activity> modelList) {
        final List<ActivityDto> result = new ArrayList<>();
        for (final Activity activity : modelList) {
            result.add(transformToDto(activity, false));
        }
        return result;
    }

    public List<ActivityDto> transformToDtoList(final List<Activity> modelList, final ActivitySearchOptions searchOptions) {
        final List<ActivityDto> result = new ArrayList<>();
        for (final Activity activity : modelList) {
            result.add(transformToDto(activity, false, searchOptions));
        }
        return result;
    }

    public ActivityDto transformToDto(Activity model, boolean detailed) {
        final BigDecimal currentPrice;
        List<ActivityPriceHistoryEntry> priceHistory = model.getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        if (!priceHistory.isEmpty()) {
            currentPrice = priceHistory.get(priceHistory.size() - 1).getPrice();
        } else {
            currentPrice = BigDecimal.ZERO;
        }
        return transformToDtoUsingPrice(model, currentPrice, detailed);
    }

    private ActivityDto transformToDtoUsingPrice(Activity model, BigDecimal currentPrice, boolean detailed) {
        List<ActivityPriceHistoryEntry> priceHistory = model.getPriceHistory();
        if (priceHistory == null) {
            priceHistory = new ArrayList<>();
        }
        return ActivityDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .approximatelyDuration(model.getApproximatelyDuration())
                .currentPrice(currentPrice)
                .relevantForPriceList(model.isRelevantForPriceList())
                .category(model.getCategory() != null ? new ActivityCategoryToActivityCategoryDtoTransformer().transformToDto(model.getCategory()) : null)
                .priceHistory(detailed ? new ActivityPriceHistoryEntryToPriceHistoryDtoTransformer().transformToDtoList(priceHistory) : null)
                .build();
    }

    public ActivityDto transformToDto(final Activity model, final boolean detailed, final ActivitySearchOptions searchOptions) {
        if (searchOptions.getPriceDate() != null && searchOptions.getPriceTime() != null) {
            final BigDecimal currentPrice = ActivityPriceUtils.getPriceFromHistory(model, searchOptions.getPriceDate().atTime(searchOptions.getPriceTime()));
            return transformToDtoUsingPrice(model, currentPrice, detailed);
        } else {
            return transformToDto(model, detailed);
        }
    }

    public ActivityDto transformToDto(final Activity model) {
        return transformToDto(model, true);
    }

    public Activity transformToEntity(final ActivityDto dtoModel) {
        return Activity.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .approximatelyDuration(dtoModel.getApproximatelyDuration())
                .relevantForPriceList(Boolean.TRUE.equals(dtoModel.getRelevantForPriceList()))
                .category(dtoModel.getCategory() != null ? new ActivityCategoryToActivityCategoryDtoTransformer().transformToEntity(dtoModel.getCategory()) : null)
                .build();
    }

    public ActivityPriceHistoryEntry createNewPriceEntry(final ActivityDto updatedDto, final Activity mergedActivity) {
        return ActivityPriceHistoryEntry.builder()
                .price(updatedDto.getCurrentPrice())
                .activity(mergedActivity)
                .build();
    }
}
