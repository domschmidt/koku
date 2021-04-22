package de.domschmidt.koku.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceHistoryDto {

    BigDecimal price;
    LocalDateTime recorded;

}
