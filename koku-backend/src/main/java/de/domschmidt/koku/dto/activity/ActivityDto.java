package de.domschmidt.koku.dto.activity;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.PriceHistoryDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActivityDto {

    private Long id;
    String description;
    Duration approximatelyDuration;
    BigDecimal currentPrice;
    List<PriceHistoryDto> priceHistory;

}
