package de.domschmidt.koku.product.kafka.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    List<ProductPriceHistoryKafkaDto> priceHistory;
    Long manufacturerId;

    LocalDateTime updated;
    LocalDateTime recorded;

}
