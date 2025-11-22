package de.domschmidt.koku.activity.transformer;

import de.domschmidt.koku.activity.persistence.Activity;
import de.domschmidt.koku.activity.persistence.ActivityPriceHistoryEntry;
import de.domschmidt.koku.dto.activity.KokuActivityDto;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;

@Component
public class ActivityToActivityDtoTransformer {

    public KokuActivityDto transformToDto(final Activity model) {
        return KokuActivityDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .name(model.getName())
                .approximatelyDuration(model.getApproximatelyDuration() != null ? LocalTime.MIN.plusNanos(model.getApproximatelyDuration().toNanos()) : null)
                .price(!model.getPriceHistory().isEmpty() ? model.getPriceHistory().getLast().getPrice() : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    public Activity transformToEntity(
            final Activity model,
            final KokuActivityDto updatedDto
    ) {

        if (updatedDto.getName() != null) {
            model.setName(updatedDto.getName());
        }
        if (updatedDto.getApproximatelyDuration() != null) {
            model.setApproximatelyDuration(Duration.ZERO.plusNanos(updatedDto.getApproximatelyDuration().toNanoOfDay()));
        }
        if (updatedDto.getPrice() != null && (model.getPriceHistory().isEmpty() || !updatedDto.getPrice().equals(model.getPriceHistory().getLast().getPrice()))) {
            model.getPriceHistory().add(new ActivityPriceHistoryEntry(
                    model,
                    updatedDto.getPrice()
            ));
        }

        return model;
    }
}
