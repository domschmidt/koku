package de.domschmidt.koku.product.kafka.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder

@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceHistoryKafkaDto {

    BigDecimal price;

    LocalDateTime recorded;

}
