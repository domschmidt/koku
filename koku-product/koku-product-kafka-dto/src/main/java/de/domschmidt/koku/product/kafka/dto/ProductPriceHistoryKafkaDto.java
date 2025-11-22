package de.domschmidt.koku.product.kafka.dto;

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
public class ProductPriceHistoryKafkaDto {

    BigDecimal price;

    LocalDateTime recorded;

}
