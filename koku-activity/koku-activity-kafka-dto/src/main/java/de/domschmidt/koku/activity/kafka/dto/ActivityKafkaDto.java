package de.domschmidt.koku.activity.kafka.dto;


import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityKafkaDto {

    public static final String TOPIC = "activities";

    Long id;

    Boolean deleted;
    String name;

    List<ActivityPriceHistoryKafkaDto> priceHistory;
    Duration approximatelyDuration;
    Long manufacturerId;

    LocalDateTime updated;
    LocalDateTime recorded;

}
