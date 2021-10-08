package de.domschmidt.koku.controller.dashboard;

import de.domschmidt.koku.dto.KokuColor;
import de.domschmidt.koku.dto.charts.*;
import de.domschmidt.koku.dto.dashboard.*;
import de.domschmidt.koku.dto.statistic.StatisticsCurrentMonthInfo;
import de.domschmidt.koku.persistence.dao.CustomerAppointmentRepository;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.service.IRevenueService;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@RestController
public class DashboardController {

    public static final DecimalFormat PERCENTAGE_DECIMAL_FORMAT;

    static {
        final DecimalFormat percentageDecimalFormat = new DecimalFormat("#");
        percentageDecimalFormat.setPositivePrefix("+");
        percentageDecimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        PERCENTAGE_DECIMAL_FORMAT = percentageDecimalFormat;
    }

    private final IRevenueService revenueService;
    private final CustomerAppointmentRepository customerAppointmentRepository;

    public DashboardController(
            final IRevenueService revenueService,
            final CustomerAppointmentRepository customerAppointmentRepository
    ) {
        this.revenueService = revenueService;
        this.customerAppointmentRepository = customerAppointmentRepository;
    }

    @GetMapping("/dashboard/panels/revenues/total")
    public IDashboardColumnContent getTotalRevenuePanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth lastMonth = currentMonth.minusMonths(1);
        final YearMonth startCurrentYear = currentMonth.minusMonths(2);
        final YearMonth endCurrentYear = currentMonth.plusMonths(2);
        final YearMonth currentMonthLastYear = YearMonth.now().minusYears(1);
        final YearMonth startLastYear = currentMonthLastYear.minusMonths(2);
        final YearMonth endLastYear = currentMonthLastYear.plusMonths(2);

        final List<BigDecimal> totalRevenuesCurrentYear = new ArrayList<>();
        final List<BigDecimal> totalRevenuesLastYear = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        final StatisticsCurrentMonthInfo statisticsForCurrentMonth = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsUntilToday = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                LocalDateTime.now()
        );
        final StatisticsCurrentMonthInfo statisticsForLastMonth = this.revenueService.generateStatisticsForRange(
                lastMonth.atDay(1).atStartOfDay(),
                lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsForCurrentMonthLastYear = this.revenueService.generateStatisticsForRange(
                currentMonthLastYear.atDay(1).atStartOfDay(),
                currentMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
        );

        final BigDecimal diffLastYear;
        final BigDecimal diffLastMonth;

        if (statisticsUntilToday.getTotal().compareTo(BigDecimal.ZERO) > 0) {
            if (statisticsForCurrentMonthLastYear.getTotal().compareTo(BigDecimal.ZERO) > 0) {
                diffLastYear = ((statisticsUntilToday.getTotal().subtract(statisticsForCurrentMonthLastYear.getTotal())).divide(statisticsForCurrentMonthLastYear.getTotal().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForCurrentMonthLastYear.getTotal(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastYear = BigDecimal.ZERO;
            }
            if (statisticsForLastMonth.getTotal().compareTo(BigDecimal.ZERO) > 0) {
                diffLastMonth = ((statisticsUntilToday.getTotal().subtract(statisticsForLastMonth.getTotal())).divide(statisticsForLastMonth.getTotal().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForLastMonth.getTotal(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastMonth = BigDecimal.ZERO;
            }
        } else {
            diffLastYear = BigDecimal.ZERO;
            diffLastMonth = BigDecimal.ZERO;
        }

        // currentYear
        YearMonth currentLoopMonth = startCurrentYear;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            totalRevenuesCurrentYear.add(statisticsForDateRange.getTotal());

            final boolean isStartTargetOrEndMonth = currentLoopMonth.equals(startCurrentYear)
                    || currentLoopMonth.equals(currentMonth)
                    || currentLoopMonth.equals(endCurrentYear);
            if (isStartTargetOrEndMonth) {
                labels.add(currentLoopMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN));
            } else {
                labels.add("");
            }

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        YearMonth currentLoopMonthLastYear = startLastYear;
        while (!currentLoopMonthLastYear.isAfter(endLastYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonthLastYear.atDay(1).atStartOfDay(),
                    currentLoopMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
            );
            totalRevenuesLastYear.add(statisticsForDateRange.getTotal());

            currentLoopMonthLastYear = currentLoopMonthLastYear.plusMonths(1);
        }


        final SegmentedData[] data = new SegmentedData[5];
        data[3] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        data[4] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();

        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(totalRevenuesCurrentYear)
                                        .segmentedData(Arrays.asList(data))
                                        .fill(true)
                                        .label(String.valueOf(currentMonth.getYear()))
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .data(totalRevenuesLastYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .label(String.valueOf(currentMonthLastYear.getYear()))
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder()
                                        .display(false)
                                        .build()
                                )
                                .x(ChartScaleConfig.builder()
                                        .display(true)
                                        .build()
                                )
                                .build()
                        )
                        .elements(ChartElementsOptions.builder()
                                .point(ChartElementsPointOptions.builder()
                                        .radius(BigDecimal.ZERO)
                                        .hoverRadius(BigDecimal.ZERO)
                                        .build()
                                )
                                .build()
                        )
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(true)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .overlay(ChartTextOverlay.builder()
                        .text(statisticsForCurrentMonth.getTotal() + " € (" + statisticsUntilToday.getTotal() + " € offen)")
                        .subline("Letztes Jahr: " + statisticsForCurrentMonthLastYear.getTotal() + " € " + (diffLastYear.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastYear) + "%)" : ""))
                        .subsubline("Letzter Monat: " + statisticsForLastMonth.getTotal() + " € " + (diffLastMonth.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastMonth) + "%)" : ""))
                        .build()
                )
                .label("Gesamtumsatz " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/panels/revenues/products")
    public IDashboardColumnContent getProductRevenuePanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth lastMonth = currentMonth.minusMonths(1);
        final YearMonth startCurrentYear = currentMonth.minusMonths(2);
        final YearMonth endCurrentYear = currentMonth.plusMonths(2);
        final YearMonth currentMonthLastYear = YearMonth.now().minusYears(1);
        final YearMonth startLastYear = currentMonthLastYear.minusMonths(2);
        final YearMonth endLastYear = currentMonthLastYear.plusMonths(2);

        final List<BigDecimal> productRevenuesCurrentYear = new ArrayList<>();
        final List<BigDecimal> productRevenuesLastYear = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        final StatisticsCurrentMonthInfo statisticsForCurrentMonth = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsUntilToday = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                LocalDateTime.now()
        );
        final StatisticsCurrentMonthInfo statisticsForLastMonth = this.revenueService.generateStatisticsForRange(
                lastMonth.atDay(1).atStartOfDay(),
                lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsForCurrentMonthLastYear = this.revenueService.generateStatisticsForRange(
                currentMonthLastYear.atDay(1).atStartOfDay(),
                currentMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
        );

        final BigDecimal diffLastYear;
        final BigDecimal diffLastMonth;

        if (statisticsForCurrentMonth.getProducts().compareTo(BigDecimal.ZERO) > 0) {
            if (statisticsForCurrentMonthLastYear.getProducts().compareTo(BigDecimal.ZERO) > 0) {
                diffLastYear = ((statisticsForCurrentMonth.getProducts().subtract(statisticsForCurrentMonthLastYear.getProducts())).divide(statisticsForCurrentMonthLastYear.getProducts().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForCurrentMonthLastYear.getProducts(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastYear = BigDecimal.ZERO;
            }
            if (statisticsForLastMonth.getProducts().compareTo(BigDecimal.ZERO) > 0) {
                diffLastMonth = ((statisticsForCurrentMonth.getProducts().subtract(statisticsForLastMonth.getProducts())).divide(statisticsForLastMonth.getProducts().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForLastMonth.getProducts(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastMonth = BigDecimal.ZERO;
            }
        } else {
            diffLastYear = BigDecimal.ZERO;
            diffLastMonth = BigDecimal.ZERO;
        }

        // currentYear
        YearMonth currentLoopMonth = startCurrentYear;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            productRevenuesCurrentYear.add(statisticsForDateRange.getProducts());

            final boolean isStartTargetOrEndMonth = currentLoopMonth.equals(startCurrentYear)
                    || currentLoopMonth.equals(currentMonth)
                    || currentLoopMonth.equals(endCurrentYear);
            if (isStartTargetOrEndMonth) {
                labels.add(currentLoopMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN));
            } else {
                labels.add("");
            }

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        YearMonth currentLoopMonthLastYear = startLastYear;
        while (!currentLoopMonthLastYear.isAfter(endLastYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonthLastYear.atDay(1).atStartOfDay(),
                    currentLoopMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
            );
            productRevenuesLastYear.add(statisticsForDateRange.getProducts());

            currentLoopMonthLastYear = currentLoopMonthLastYear.plusMonths(1);
        }


        final SegmentedData[] data = new SegmentedData[5];
        data[3] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        data[4] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();

        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(productRevenuesCurrentYear)
                                        .segmentedData(Arrays.asList(data))
                                        .fill(true)
                                        .label(String.valueOf(currentMonth.getYear()))
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .data(productRevenuesLastYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .label(String.valueOf(currentMonthLastYear.getYear()))
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder()
                                        .display(false)
                                        .build()
                                )
                                .x(ChartScaleConfig.builder()
                                        .display(true)
                                        .build()
                                )
                                .build()
                        )
                        .elements(ChartElementsOptions.builder()
                                .point(ChartElementsPointOptions.builder()
                                        .radius(BigDecimal.ZERO)
                                        .hoverRadius(BigDecimal.ZERO)
                                        .build()
                                )
                                .build()
                        )
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(true)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .overlay(ChartTextOverlay.builder()
                        .text(statisticsForCurrentMonth.getProducts() + " € (" + statisticsUntilToday.getProducts() + " € offen)")
                        .subline("Letztes Jahr: " + statisticsForCurrentMonthLastYear.getProducts() + " € " + (diffLastYear.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastYear) + "%)" : ""))
                        .subsubline("Letzter Monat: " + statisticsForLastMonth.getProducts() + " € " + (diffLastMonth.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastMonth) + "%)" : ""))
                        .build()
                )
                .label("Produktumsatz " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/panels/revenues/activities")
    public IDashboardColumnContent getActivityRevenuePanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth lastMonth = currentMonth.minusMonths(1);
        final YearMonth startCurrentYear = currentMonth.minusMonths(2);
        final YearMonth endCurrentYear = currentMonth.plusMonths(2);
        final YearMonth currentMonthLastYear = YearMonth.now().minusYears(1);
        final YearMonth startLastYear = currentMonthLastYear.minusMonths(2);
        final YearMonth endLastYear = currentMonthLastYear.plusMonths(2);

        final List<BigDecimal> activityRevenuesCurrentYear = new ArrayList<>();
        final List<BigDecimal> activityRevenuesLastYear = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        final StatisticsCurrentMonthInfo statisticsForCurrentMonth = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsUntilToday = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                LocalDateTime.now()
        );
        final StatisticsCurrentMonthInfo statisticsForLastMonth = this.revenueService.generateStatisticsForRange(
                lastMonth.atDay(1).atStartOfDay(),
                lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo statisticsForCurrentMonthLastYear = this.revenueService.generateStatisticsForRange(
                currentMonthLastYear.atDay(1).atStartOfDay(),
                currentMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
        );

        final BigDecimal diffLastYear;
        final BigDecimal diffLastMonth;

        if (statisticsForCurrentMonth.getActivities().compareTo(BigDecimal.ZERO) > 0) {
            if (statisticsForCurrentMonthLastYear.getActivities().compareTo(BigDecimal.ZERO) > 0) {
                diffLastYear = ((statisticsForCurrentMonth.getActivities().subtract(statisticsForCurrentMonthLastYear.getActivities())).divide(statisticsForCurrentMonthLastYear.getActivities().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForCurrentMonthLastYear.getActivities(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastYear = BigDecimal.ZERO;
            }
            if (statisticsForLastMonth.getActivities().compareTo(BigDecimal.ZERO) > 0) {
                diffLastMonth = ((statisticsForCurrentMonth.getActivities().subtract(statisticsForLastMonth.getActivities())).divide(statisticsForLastMonth.getActivities().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForLastMonth.getActivities(), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastMonth = BigDecimal.ZERO;
            }
        } else {
            diffLastYear = BigDecimal.ZERO;
            diffLastMonth = BigDecimal.ZERO;
        }

        // currentYear
        YearMonth currentLoopMonth = startCurrentYear;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            activityRevenuesCurrentYear.add(statisticsForDateRange.getActivities());

            final boolean isStartTargetOrEndMonth = currentLoopMonth.equals(startCurrentYear)
                    || currentLoopMonth.equals(currentMonth)
                    || currentLoopMonth.equals(endCurrentYear);
            if (isStartTargetOrEndMonth) {
                labels.add(currentLoopMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN));
            } else {
                labels.add("");
            }

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        YearMonth currentLoopMonthLastYear = startLastYear;
        while (!currentLoopMonthLastYear.isAfter(endLastYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonthLastYear.atDay(1).atStartOfDay(),
                    currentLoopMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
            );
            activityRevenuesLastYear.add(statisticsForDateRange.getActivities());

            currentLoopMonthLastYear = currentLoopMonthLastYear.plusMonths(1);
        }


        final SegmentedData[] data = new SegmentedData[5];
        data[3] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        data[4] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();

        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(activityRevenuesCurrentYear)
                                        .segmentedData(Arrays.asList(data))
                                        .fill(true)
                                        .label(String.valueOf(currentMonth.getYear()))
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .build(),
                                ChartDataSet.builder()
                                        .data(activityRevenuesLastYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .label(String.valueOf(currentMonthLastYear.getYear()))
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder()
                                        .display(false)
                                        .build()
                                )
                                .x(ChartScaleConfig.builder()
                                        .display(true)
                                        .build()
                                )
                                .build()
                        )
                        .elements(ChartElementsOptions.builder()
                                .point(ChartElementsPointOptions.builder()
                                        .radius(BigDecimal.ZERO)
                                        .hoverRadius(BigDecimal.ZERO)
                                        .build()
                                )
                                .build()
                        )
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(true)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .overlay(ChartTextOverlay.builder()
                        .text(statisticsForCurrentMonth.getActivities() + " € (" + statisticsUntilToday.getActivities() + " € offen)")
                        .subline("Letztes Jahr: " + statisticsForCurrentMonthLastYear.getActivities() + " € " + (diffLastYear.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastYear) + "%)" : ""))
                        .subsubline("Letzter Monat: " + statisticsForLastMonth.getActivities() + " € " + (diffLastMonth.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastMonth) + "%)" : ""))
                        .build()
                )
                .label("Tätigkeitsumsatz " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/panels/revenues/expected")
    public IDashboardColumnContent getExpectedRevenuePanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth endCurrentYear = currentMonth.plusMonths(5);

        final List<TableRow> rows = new ArrayList<>();

        // currentYear
        YearMonth currentLoopMonth = currentMonth;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final StatisticsCurrentMonthInfo statisticsForDateRange = this.revenueService.generateStatisticsForRange(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            rows.add(TableRow.builder()
                    .cells(Arrays.asList(
                            StringTableRowCell.builder()
                                    .value(currentLoopMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentLoopMonth.getYear())
                                    .build(),
                            StringTableRowCell.builder()
                                    .value(statisticsForDateRange.getTotal() + " €")
                                    .build()
                    ))
                    .build());

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        return TableDashboardColumnContent.builder()
                .label("Umsatzerwartungen")
                .columns(Arrays.asList(
                        TableColumn.builder()
                                .label("Monat")
                                .build(),
                        TableColumn.builder()
                                .label("Erwarteter Umsatz")
                                .build()
                ))
                .rows(rows)
                .build();
    }

    @GetMapping("/dashboard/panels/customers/appointments/expected")
    public IDashboardColumnContent getExpectedCustomerAppointmentsPanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth endCurrentYear = currentMonth.plusMonths(5);

        final List<TableRow> rows = new ArrayList<>();

        // currentYear
        YearMonth currentLoopMonth = currentMonth;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final List<CustomerAppointment> customerAppointments = this.customerAppointmentRepository.findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                    currentLoopMonth.atDay(1).atStartOfDay(),
                    currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
            );
            rows.add(TableRow.builder()
                    .cells(Arrays.asList(
                            StringTableRowCell.builder()
                                    .value(currentLoopMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentLoopMonth.getYear())
                                    .build(),
                            StringTableRowCell.builder()
                                    .value(String.valueOf(customerAppointments.size()))
                                    .build()
                    ))
                    .build());

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        return TableDashboardColumnContent.builder()
                .label("Terminerwartungen")
                .columns(Arrays.asList(
                        TableColumn.builder()
                                .label("Monat")
                                .build(),
                        TableColumn.builder()
                                .label("Erwartete Kundentermine")
                                .build()
                ))
                .rows(rows)
                .build();
    }

    @GetMapping("/dashboard/panels/customers/new")
    public IDashboardColumnContent getNewCustomersPanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth lastMonth = currentMonth.minusMonths(1);
        final YearMonth startCurrentYear = currentMonth.minusMonths(2);
        final YearMonth endCurrentYear = currentMonth.plusMonths(2);
        final YearMonth currentMonthLastYear = YearMonth.now().minusYears(1);
        final YearMonth startLastYear = currentMonthLastYear.minusMonths(2);
        final YearMonth endLastYear = currentMonthLastYear.plusMonths(2);

        final List<BigDecimal> newCustomerCountsCurrentYear = new ArrayList<>();
        final List<BigDecimal> newCustomerCountsLastYear = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        final BigDecimal customerCountHavingFirstCustomerAppointmentStartInCurrentMonth =
                new BigDecimal(this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                        currentMonth.atDay(1).atStartOfDay(),
                        currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal customerCountHavingFirstCustomerAppointmentStartInLastMonth =
                new BigDecimal(this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                        lastMonth.atDay(1).atStartOfDay(),
                        lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear =
                new BigDecimal(this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                        currentMonthLastYear.atDay(1).atStartOfDay(),
                        currentMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal diffLastYear;
        final BigDecimal diffLastMonth;

        if (customerCountHavingFirstCustomerAppointmentStartInCurrentMonth.compareTo(BigDecimal.ZERO) > 0) {
            if (customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear.compareTo(BigDecimal.ZERO) > 0) {
                diffLastYear = ((customerCountHavingFirstCustomerAppointmentStartInCurrentMonth.subtract(customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear)).divide(customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastYear = BigDecimal.ZERO;
            }
            if (customerCountHavingFirstCustomerAppointmentStartInLastMonth.compareTo(BigDecimal.ZERO) > 0) {
                diffLastMonth = ((customerCountHavingFirstCustomerAppointmentStartInCurrentMonth.subtract(customerCountHavingFirstCustomerAppointmentStartInLastMonth)).divide(customerCountHavingFirstCustomerAppointmentStartInLastMonth.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : customerCountHavingFirstCustomerAppointmentStartInLastMonth, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastMonth = BigDecimal.ZERO;
            }
        } else {
            diffLastYear = BigDecimal.ZERO;
            diffLastMonth = BigDecimal.ZERO;
        }

        // currentYear
        YearMonth currentLoopMonth = startCurrentYear;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final List<Long> customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonth =
                    this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                            currentLoopMonth.atDay(1).atStartOfDay(),
                            currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
                    );
            newCustomerCountsCurrentYear.add(new BigDecimal(customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonth.size()));

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        YearMonth currentLoopMonthLastYear = startLastYear;
        while (!currentLoopMonthLastYear.isAfter(endLastYear)) {
            final List<Long> customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonthLastYear =
                    this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                            currentLoopMonthLastYear.atDay(1).atStartOfDay(),
                            currentLoopMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
                    );
            newCustomerCountsLastYear.add(new BigDecimal(customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonthLastYear.size()));

            final boolean isStartTargetOrEndMonth = currentLoopMonthLastYear.equals(startLastYear)
                    || currentLoopMonthLastYear.equals(currentMonthLastYear)
                    || currentLoopMonthLastYear.equals(endLastYear);
            if (isStartTargetOrEndMonth) {
                labels.add(currentLoopMonthLastYear.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN));
            } else {
                labels.add("");
            }

            currentLoopMonthLastYear = currentLoopMonthLastYear.plusMonths(1);
        }
        final SegmentedData[] data = new SegmentedData[5];
        data[3] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        data[4] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();

        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(newCustomerCountsCurrentYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .segmentedData(Arrays.asList(data))
                                        .label(String.valueOf(currentMonth.getYear()))
                                        .build(),
                                ChartDataSet.builder()
                                        .data(newCustomerCountsLastYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .label(String.valueOf(currentMonthLastYear.getYear()))
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder()
                                        .display(false)
                                        .build()
                                )
                                .x(ChartScaleConfig.builder()
                                        .display(true)
                                        .build()
                                )
                                .build()
                        )
                        .elements(ChartElementsOptions.builder()
                                .point(ChartElementsPointOptions.builder()
                                        .radius(BigDecimal.ZERO)
                                        .hoverRadius(BigDecimal.ZERO)
                                        .build()
                                )
                                .build()
                        )
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(true)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .overlay(ChartTextOverlay.builder()
                        .text(customerCountHavingFirstCustomerAppointmentStartInCurrentMonth + " Neukunde(n)")
                        .subline("Letztes Jahr: " + customerCountHavingFirstCustomerAppointmentStartInCurrentMonthLastYear + " " + (diffLastYear.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastYear) + "%)" : ""))
                        .subsubline("Letzter Monat: " + customerCountHavingFirstCustomerAppointmentStartInLastMonth + " " + (diffLastMonth.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastMonth) + "%)" : ""))
                        .build()
                )
                .label("Neukunden " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/panels/customers/appointments")
    public IDashboardColumnContent getCustomerAppointmentsPanel() {
        final YearMonth currentMonth = YearMonth.now();
        final YearMonth lastMonth = currentMonth.minusMonths(1);
        final YearMonth startCurrentYear = currentMonth.minusMonths(2);
        final YearMonth endCurrentYear = currentMonth.plusMonths(2);
        final YearMonth currentMonthLastYear = YearMonth.now().minusYears(1);
        final YearMonth startLastYear = currentMonthLastYear.minusMonths(2);
        final YearMonth endLastYear = currentMonthLastYear.plusMonths(2);

        final List<BigDecimal> customerAppointmentCountCurrentYear = new ArrayList<>();
        final List<BigDecimal> customerAppointmentCountLastYear = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        final BigDecimal customerAppointmentCountCurrentMonthTotal =
                new BigDecimal(this.customerAppointmentRepository.findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                        currentMonth.atDay(1).atStartOfDay(),
                        currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());
        final BigDecimal customerAppointmentCountCurrentMonthOpen =
                new BigDecimal(this.customerAppointmentRepository.findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                        LocalDateTime.now(),
                        currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal customerAppointmentCountLastMonthTotal =
                new BigDecimal(this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                        lastMonth.atDay(1).atStartOfDay(),
                        lastMonth.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal customerAppointmentCountCurrentMonthLastYearTotal =
                new BigDecimal(this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                        currentMonthLastYear.atDay(1).atStartOfDay(),
                        currentMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
                ).size());

        final BigDecimal diffLastYear;
        final BigDecimal diffLastMonth;

        if (customerAppointmentCountCurrentMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
            if (customerAppointmentCountCurrentMonthLastYearTotal.compareTo(BigDecimal.ZERO) > 0) {
                diffLastYear = ((customerAppointmentCountCurrentMonthTotal.subtract(customerAppointmentCountCurrentMonthLastYearTotal)).divide(customerAppointmentCountCurrentMonthLastYearTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : customerAppointmentCountCurrentMonthLastYearTotal, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastYear = BigDecimal.ZERO;
            }
            if (customerAppointmentCountLastMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
                diffLastMonth = ((customerAppointmentCountCurrentMonthTotal.subtract(customerAppointmentCountLastMonthTotal)).divide(customerAppointmentCountLastMonthTotal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : customerAppointmentCountLastMonthTotal, 2, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            } else {
                diffLastMonth = BigDecimal.ZERO;
            }
        } else {
            diffLastYear = BigDecimal.ZERO;
            diffLastMonth = BigDecimal.ZERO;
        }

        // currentYear
        YearMonth currentLoopMonth = startCurrentYear;
        while (!currentLoopMonth.isAfter(endCurrentYear)) {
            final List<Long> customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonth =
                    this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                            currentLoopMonth.atDay(1).atStartOfDay(),
                            currentLoopMonth.atEndOfMonth().atTime(LocalTime.MAX)
                    );
            customerAppointmentCountCurrentYear.add(new BigDecimal(customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonth.size()));

            final boolean isStartTargetOrEndMonth = currentLoopMonth.equals(startCurrentYear)
                    || currentLoopMonth.equals(currentMonth)
                    || currentLoopMonth.equals(endCurrentYear);
            if (isStartTargetOrEndMonth) {
                labels.add(currentLoopMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.GERMAN));
            } else {
                labels.add("");
            }

            currentLoopMonth = currentLoopMonth.plusMonths(1);
        }

        YearMonth currentLoopMonthLastYear = startLastYear;
        while (!currentLoopMonthLastYear.isAfter(endLastYear)) {
            final List<Long> customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonthLastYear =
                    this.customerAppointmentRepository.findCustomerIdsHavingFirstCustomerAppointmentStartBetween(
                            currentLoopMonthLastYear.atDay(1).atStartOfDay(),
                            currentLoopMonthLastYear.atEndOfMonth().atTime(LocalTime.MAX)
                    );
            customerAppointmentCountLastYear.add(new BigDecimal(customerIdsHavingFirstCustomerAppointmentStartInCurrentLoopMonthLastYear.size()));

            currentLoopMonthLastYear = currentLoopMonthLastYear.plusMonths(1);
        }


        final SegmentedData[] data = new SegmentedData[5];
        data[3] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        data[4] = SegmentedData.builder()
                .backgroundColor(KokuColor.TRANSPARENT)
                .borderDashed(true)
                .build();
        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.LINE)
                .data(ChartData.builder()
                        .labels(labels)
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(customerAppointmentCountCurrentYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY
                                        ))
                                        .segmentedData(Arrays.asList(data))
                                        .label(String.valueOf(currentMonth.getYear()))
                                        .build(),
                                ChartDataSet.builder()
                                        .data(customerAppointmentCountLastYear)
                                        .fill(true)
                                        .colors(Arrays.asList(
                                                KokuColor.SECONDARY
                                        ))
                                        .label(String.valueOf(currentMonthLastYear.getYear()))
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder()
                                        .display(false)
                                        .build()
                                )
                                .x(ChartScaleConfig.builder()
                                        .display(true)
                                        .build()
                                )
                                .build()
                        )
                        .elements(ChartElementsOptions.builder()
                                .point(ChartElementsPointOptions.builder()
                                        .radius(BigDecimal.ZERO)
                                        .hoverRadius(BigDecimal.ZERO)
                                        .build()
                                )
                                .build()
                        )
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(true)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build())
                .overlay(ChartTextOverlay.builder()
                        .text(customerAppointmentCountCurrentMonthTotal + " Termin(e) (" + customerAppointmentCountCurrentMonthOpen + " offen)")
                        .subline("Letztes Jahr: " + customerAppointmentCountCurrentMonthLastYearTotal + " " + (diffLastYear.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastYear) + "%)" : ""))
                        .subsubline("Letzter Monat: " + customerAppointmentCountLastMonthTotal + " " + (diffLastMonth.compareTo(BigDecimal.ZERO) != 0 ? "(" + PERCENTAGE_DECIMAL_FORMAT.format(diffLastMonth) + "%)" : ""))
                        .build()
                )
                .label("Kundentermine " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/panels/customers/revenues")
    public IDashboardColumnContent getCustomerRevenuesPanel() {
        final Year currentYear = Year.now();
        final LocalDateTime startCurrentYear = currentYear.atMonth(1).atDay(1).atStartOfDay();
        final LocalDateTime endCurrentYear = currentYear.atMonth(12).atEndOfMonth().atTime(LocalTime.MAX);

        Map<Customer, BigDecimal> customerRevenueMap = new HashMap<>();

        final List<CustomerAppointment> allRelevantAppointments = this.customerAppointmentRepository.findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                startCurrentYear,
                endCurrentYear
        );

        for (final CustomerAppointment currentAppointment : allRelevantAppointments) {
            BigDecimal activityRevenueSum = BigDecimal.ZERO;
            BigDecimal productRevenueSum = BigDecimal.ZERO;

            for (final CustomerAppointmentActivity activity : currentAppointment.getActivities()) {
                activityRevenueSum = activityRevenueSum.add(ActivityPriceUtils.getActivityPriceForCustomerAppointment(activity, currentAppointment));
            }

            for (final CustomerAppointmentSoldProduct soldProduct : currentAppointment.getSoldProducts()) {
                productRevenueSum = productRevenueSum.add(ProductPriceUtils.getSoldProductPriceForCustomerAppointment(soldProduct, currentAppointment));
            }

            final BigDecimal totalAppointmentRevenueSum = activityRevenueSum.add(productRevenueSum);

            if (totalAppointmentRevenueSum.compareTo(BigDecimal.ZERO) > 0) {
                if (customerRevenueMap.containsKey(currentAppointment.getCustomer())) {
                    customerRevenueMap.put(currentAppointment.getCustomer(), customerRevenueMap.get(currentAppointment.getCustomer()).add(totalAppointmentRevenueSum));
                } else {
                    customerRevenueMap.put(currentAppointment.getCustomer(), totalAppointmentRevenueSum);
                }
            }
        }

        final List<Map.Entry<Customer, BigDecimal>> sortedCustomerList = new ArrayList<>(customerRevenueMap.entrySet());
        final Comparator<Map.Entry<Customer, BigDecimal>> entryComparator = Map.Entry.comparingByValue();
        sortedCustomerList.sort(entryComparator.reversed());

        final List<TableRow> rows = new ArrayList<>();

        for (int i = 0; i <= Math.min(sortedCustomerList.size() - 1, 9); i++) {
            final Map.Entry<Customer, BigDecimal> entry = sortedCustomerList.get(i);
            rows.add(TableRow.builder()
                    .cells(Arrays.asList(
                            StringTableRowCell.builder()
                                    .value(entry.getKey().getFirstName() + " " + entry.getKey().getLastName())
                                    .build(),
                            StringTableRowCell.builder()
                                    .value(entry.getValue().setScale(2, RoundingMode.HALF_UP) + " €")
                                    .build()
                    ))
                    .build());
        }

        return TableDashboardColumnContent.builder()
                .label("Top 10 Kunden " + currentYear.getValue())
                .columns(Arrays.asList(
                        TableColumn.builder()
                                .label("Kunde")
                                .build(),
                        TableColumn.builder()
                                .label("Umsatz")
                                .build()
                ))
                .rows(rows)
                .build();
    }

    @GetMapping("/dashboard/panels/revenues/segmentation")
    public IDashboardColumnContent getSegmentationRevenuesPanel() {
        final YearMonth currentMonth = YearMonth.now();
        final StatisticsCurrentMonthInfo statisticsForCurrentMonth = this.revenueService.generateStatisticsForRange(
                currentMonth.atDay(1).atStartOfDay(),
                currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
        );
        final BigDecimal productsSegment = statisticsForCurrentMonth.getProducts().divide(statisticsForCurrentMonth.getTotal().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForCurrentMonth.getTotal(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        final BigDecimal activitiesSegment = statisticsForCurrentMonth.getActivities().divide(statisticsForCurrentMonth.getTotal().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : statisticsForCurrentMonth.getTotal(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));

        final boolean anyValueAvailable = productsSegment.compareTo(BigDecimal.ZERO) != 0 || activitiesSegment.compareTo(BigDecimal.ZERO) != 0;

        return DiagramDashboardColumnContent.builder()
                .type(ChartTypeEnum.PIE)
                .data(ChartData.builder()
                        .labels(Arrays.asList(
                                "Produkte", "Tätigkeiten"
                        ))
                        .datasets(Arrays.asList(
                                ChartDataSet.builder()
                                        .data(anyValueAvailable ? Arrays.asList(
                                                productsSegment, activitiesSegment
                                        ) : null)
                                        .colors(Arrays.asList(
                                                KokuColor.PRIMARY,
                                                KokuColor.SECONDARY,
                                                KokuColor.TERTIARY
                                        ))
                                        .datalabels(ChartDataLabelsConfig.builder()
                                                .color("#000000")
                                                .textAlign(DataLabelsTextAlignEnum.CENTER)
                                                .formatter("%LABEL%\n%VALUE% %")
                                                .build())
                                        .build()
                        ))
                        .build())
                .options(ChartOptions.builder()
                        .scales(ChartScalesOptions.builder()
                                .y(ChartScaleConfig.builder().display(false).build())
                                .x(ChartScaleConfig.builder().display(false).build())
                                .build())
                        .plugins(ChartPluginOptions.builder()
                                .legend(ChartPluginLegendOptions.builder()
                                        .display(false)
                                        .build()
                                )
                                .tooltip(ChartPluginTooltipOptions.builder()
                                        .enabled(false)
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .label("Umsatzaufteilung " + currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN) + " " + currentMonth.getYear())
                .build();
    }

    @GetMapping("/dashboard/config")
    public DashboardConfigDto getConfig() {
        return DashboardConfigDto.builder()
                .columns(Arrays.asList(
                        DashboardColumnConfigDto.builder()
                                .label("Umsätze")
                                .xsWidthPercentage(new BigDecimal("100"))
                                .smWidthPercentage(new BigDecimal("50"))
                                .mdWidthPercentage(new BigDecimal("33.33333333"))
                                .contents(Arrays.asList(
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/revenues/total")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/revenues/products")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/revenues/activities")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/revenues/segmentation")
                                                .build()
                                ))
                                .build(),
                        DashboardColumnConfigDto.builder()
                                .label("Erwartungen")
                                .xsWidthPercentage(new BigDecimal("100"))
                                .smWidthPercentage(new BigDecimal("50"))
                                .mdWidthPercentage(new BigDecimal("33.33333333"))
                                .contents(Arrays.asList(
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/revenues/expected")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/customers/appointments/expected")
                                                .build()
                                ))
                                .build(),
                        DashboardColumnConfigDto.builder()
                                .label("Kunden")
                                .xsWidthPercentage(new BigDecimal("100"))
                                .smWidthPercentage(new BigDecimal("50"))
                                .mdWidthPercentage(new BigDecimal("33.33333333"))
                                .contents(Arrays.asList(
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/customers/revenues")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/customers/new")
                                                .build(),
                                        DeferredDashboardColumnContent.builder()
                                                .href("/dashboard/panels/customers/appointments")
                                                .build()
                                ))
                                .build()
                ))
                .build();
    }

}
