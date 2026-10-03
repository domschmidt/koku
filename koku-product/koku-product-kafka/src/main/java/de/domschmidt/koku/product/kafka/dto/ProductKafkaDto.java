package de.domschmidt.koku.product.kafka.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductKafkaDto {

    public static final String TOPIC = "products";

    Long id;

    Boolean deleted;
    String name;
    Integer milliliters;

    List<ProductPriceHistoryKafkaDto> priceHistory;
    Long manufacturerId;

    LocalDateTime updated;
    LocalDateTime recorded;
}
