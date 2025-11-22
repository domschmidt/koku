package de.domschmidt.koku.activity.kafka.activity.transformer;


import de.domschmidt.koku.activity.kafka.dto.ActivityKafkaDto;
import de.domschmidt.koku.activity.kafka.dto.ActivityPriceHistoryKafkaDto;
import de.domschmidt.koku.activity.persistence.Activity;

public class ActivityToKafkaActivityDtoTransformer {

    public ActivityKafkaDto transformToDto(final Activity model) {
        return ActivityKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .name(model.getName())
                .updated(model.getUpdated())
                .priceHistory(model.getPriceHistory().stream().map(activityPriceHistoryEntry -> ActivityPriceHistoryKafkaDto.builder()
                        .price(activityPriceHistoryEntry.getPrice())
                        .recorded(activityPriceHistoryEntry.getRecorded())
                        .build()
                ).toList())
                .approximatelyDuration(model.getApproximatelyDuration())
                .recorded(model.getRecorded())
                .build();
    }
}
