package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.PriceHistoryDto;
import de.domschmidt.koku.persistence.model.ActivityPriceHistoryEntry;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActivityPriceHistoryEntryToPriceHistoryDtoTransformer implements ITransformer<ActivityPriceHistoryEntry, PriceHistoryDto> {

    @Override
    public List<PriceHistoryDto> transformToDtoList(final List<ActivityPriceHistoryEntry> modelList) {
        final List<PriceHistoryDto> result = new ArrayList<>();
        for (final ActivityPriceHistoryEntry activityPriceHistoryEntry : modelList) {
            result.add(transformToDto(activityPriceHistoryEntry));
        }
        return result;
    }

    @Override
    public PriceHistoryDto transformToDto(final ActivityPriceHistoryEntry model) {
        return PriceHistoryDto.builder()
                .price(model.getPrice())
                .recorded(model.getRecorded())
                .build();
    }

    @Override
    public ActivityPriceHistoryEntry transformToEntity(final PriceHistoryDto dtoModel) {
        return ActivityPriceHistoryEntry.builder()
                .price(dtoModel.getPrice())
                .build();
    }
}
