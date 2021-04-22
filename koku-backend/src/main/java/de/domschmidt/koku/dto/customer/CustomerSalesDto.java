package de.domschmidt.koku.dto.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.product.ProductDto;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerSalesDto {

    private LocalDate startDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    private List<ProductDto> soldProducts;

}
