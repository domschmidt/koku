package de.domschmidt.koku.dto.statistic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsDto {

    LocalDateTime generated;
    StatisticsCurrentMonthInfo currentMonth;
    StatisticsLastMonthComparisonInfo lastMonthComparison;

}
