package de.domschmidt.koku.dto.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardColumnConfigDto {

    BigDecimal xsWidthPercentage;
    BigDecimal smWidthPercentage;
    BigDecimal mdWidthPercentage;
    BigDecimal lgWidthPercentage;
    BigDecimal xlWidthPercentage;

    List<IDashboardColumnContent> contents;
    String label;

}
