package de.domschmidt.koku.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.domschmidt.koku.dto.PriceHistoryDto;
import de.domschmidt.koku.dto.activity.ActivitySequenceItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto extends ActivitySequenceItemDto {

    Long id;
    ProductManufacturerDto manufacturer;
    String description;
    List<ProductCategoryDto> categories;
    BigDecimal currentPrice;
    List<PriceHistoryDto> priceHistory;

}
