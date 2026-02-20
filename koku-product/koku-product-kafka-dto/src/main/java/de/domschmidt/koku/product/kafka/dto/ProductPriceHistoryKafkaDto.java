package de.domschmidt.koku.product.kafka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistoryKafkaDto {

    BigDecimal price;

    LocalDateTime recorded;
}
