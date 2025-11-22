package de.domschmidt.koku.product.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
