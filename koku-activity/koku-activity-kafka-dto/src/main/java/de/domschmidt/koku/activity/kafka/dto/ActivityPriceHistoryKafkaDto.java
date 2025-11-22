package de.domschmidt.koku.activity.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class ActivityPriceHistoryKafkaDto {

    BigDecimal price;

    LocalDateTime recorded;

}

