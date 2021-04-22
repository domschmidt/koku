package de.domschmidt.koku.dto.charts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.YearMonth;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter

public class ChartYearMonthFilter extends ChartFilter {

    YearMonth value;

}
