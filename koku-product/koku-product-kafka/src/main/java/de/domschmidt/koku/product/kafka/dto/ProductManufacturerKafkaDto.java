package de.domschmidt.koku.product.kafka.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductManufacturerKafkaDto {

    public static final String TOPIC = "productmanufacturers";

    Long id;

    Boolean deleted;

    String name;

    LocalDateTime updated;
    LocalDateTime recorded;
}
