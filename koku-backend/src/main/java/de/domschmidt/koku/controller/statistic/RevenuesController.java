package de.domschmidt.koku.controller.statistic;

import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.charts.ChartData;
import de.domschmidt.koku.dto.charts.ChartDataSet;
import de.domschmidt.koku.dto.charts.ChartTypeEnum;
import de.domschmidt.koku.dto.charts.ChartYearMonthFilter;
import de.domschmidt.koku.dto.panels.ChartPanelDto;
import de.domschmidt.koku.dto.statistic.StatisticsCurrentMonthInfo;
import de.domschmidt.koku.service.IRevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/revenues")
public class RevenuesController {

    private final IRevenueService revenueService;

    @Autowired
    public RevenuesController(
            final IRevenueService revenueService
    ) {
        this.revenueService = revenueService;
    }

    @GetMapping(value = "/statistics")
    public ChartPanelDto getRevenueStatistics(
            @RequestParam(required = false) YearMonth start,
            @RequestParam(required = false) YearMonth end
    ) {
        final YearMonth currentMonth = YearMonth.now();
        if (start == null) {
            start = currentMonth.minusMonths(6);
        }
        if (end == null) {
            end = currentMonth;
        }

        final List<String> labels = new ArrayList<>();
        final List<BigDecimal> productRevenues = new ArrayList<>();
        final List<BigDecimal> activityRevenues = new ArrayList<>();
        final List<BigDecimal> totalRevenues = new ArrayList<>();
        YearMonth currentLoopMonth = start;
        while (!currentLoopMonth.isAfter(end)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            labels.add(statisticsForDateRange.getName());
            productRevenues.add(statisticsForDateRange.getProducts());
            activityRevenues.add(statisticsForDateRange.getActivities());
            totalRevenues.add(statisticsForDateRange.getTotal());

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        return ChartPanelDto.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .label("Gesamt")
                                        .data(totalRevenues)
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Tätigkeiten")
                                        .data(activityRevenues)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .label("Produkte")
                                        .data(productRevenues)
                                        .colors(Arrays.asList(
                                                KokuColor.TERTIARY
                                        ))
                                        .build()
                        ))
                        .build())
                .filters(Arrays.asList(
                        ChartYearMonthFilter.builder()
                                .label("von")
                                .queryParam("start")
                                .value(start)
                                .build(),
                        ChartYearMonthFilter.builder()
                                .label("bis")
                                .queryParam("end")
                                .value(end)
                                .build()
                ))
                .build();
    }

}
