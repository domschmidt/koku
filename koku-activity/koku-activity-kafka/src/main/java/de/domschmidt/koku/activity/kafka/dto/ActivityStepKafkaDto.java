package de.domschmidt.koku.activity.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityStepKafkaDto {

    public static final String TOPIC = "activitysteps";

    Long id;

    Boolean deleted;
    String name;

    List<ActivityPriceHistoryKafkaDto> priceHistory;
    Long manufacturerId;

    LocalDateTime updated;
    LocalDateTime recorded;
}
