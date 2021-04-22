package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.product.ProductDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAppointmentSoldProductDto {

    Long id;
    ProductDto product;
    BigDecimal sellPrice;

}
