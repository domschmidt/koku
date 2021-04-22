package de.domschmidt.koku.dto.statistic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsCurrentMonthInfo {

    String name;
    BigDecimal total;
    BigDecimal products;
    BigDecimal activities;

}
