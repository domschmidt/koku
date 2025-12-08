package de.domschmidt.koku.activity.kafka.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityPriceHistoryKafkaDto {

    BigDecimal price;

    LocalDateTime recorded;

}

