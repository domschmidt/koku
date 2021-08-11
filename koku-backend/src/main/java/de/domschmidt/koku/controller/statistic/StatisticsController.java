package de.domschmidt.koku.controller.statistic;

import de.domschmidt.koku.dto.panels.GaugePanelDto;
import de.domschmidt.koku.dto.panels.TextPanelContent;
import de.domschmidt.koku.dto.panels.TextPanelDto;
import de.domschmidt.koku.dto.statistic.StatisticsCurrentMonthInfo;
import de.domschmidt.koku.persistence.dao.CustomerAppointmentRepository;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import de.domschmidt.koku.utils.NPEGuardUtils;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    private final CustomerAppointmentRepository customerAppointmentRepository;

    @Autowired
    public StatisticsController(
            final CustomerAppointmentRepository customerAppointmentRepository
    ) {
        this.customerAppointmentRepository = customerAppointmentRepository;
    }

    @GetMapping(value = "/lastmonthcomparison")
    public GaugePanelDto getLastMonthComparison() {
        return GaugePanelDto.builder()
                .percentage(generateStatisticsForLastMonthComparison())
                .title("Prozent vom letzten Monat")
                .build();
    }

    @GetMapping(value = "/monthlyapproxrevenue")
    public TextPanelDto getMonthlyApproxRevenue(final YearMonth startMonth, final Integer monthCount) {
        List<TextPanelContent> contents = new ArrayList<>();
        for (int i = 0; i < monthCount; i++) {
            final YearMonth currentMonth = startMonth.plusMonths(i);
            contents.add(TextPanelContent.builder()
                            .text(currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN))
                            .build()
            );
            contents.add(TextPanelContent.builder()
                    .text(
                            generateStatisticsForRange(
                                    currentMonth.atDay(1).atStartOfDay(),
                                    currentMonth.atEndOfMonth().atTime(LocalTime.MAX)
                            ).getTotal().toString() + "€"
                    )
                    .build()
            );
        }

        return TextPanelDto.builder()
                .texts(contents)
                .title("Erwarteter Monatsumsatz")
                .build();
    }

    @GetMapping(value = "/currentmonthapproxrevenue")
    public TextPanelDto getMonthlyRevenue() {
        final YearMonth yearMonth = YearMonth.now();
        return TextPanelDto.builder()
                .texts(Arrays.asList(
                        TextPanelContent.builder()
                                .text(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN))
                                .build(),
                        TextPanelContent.builder()
                                .text(
                                        generateStatisticsForRange(
                                                yearMonth.atDay(1).atStartOfDay(),
                                                LocalDateTime.now()
                                        ).getTotal().toString() + "€"
                                )
                                .build()
                ))
                .title("Erreichter Monatsumsatz")
                .build();
    }

    private Integer generateStatisticsForLastMonthComparison() {
        final StatisticsCurrentMonthInfo lastMonthStatistics = generateStatisticsForRange(
                YearMonth.now().minusMonths(1).atDay(1).atStartOfDay(),
                YearMonth.now().minusMonths(1).atEndOfMonth().atTime(LocalTime.MAX)
        );
        final StatisticsCurrentMonthInfo thisMonthStatistics = generateStatisticsForRange(
                YearMonth.now().atDay(1).atStartOfDay(),
                YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX)
        );

        final BigDecimal total = BigDecimal.ZERO.compareTo(NPEGuardUtils.get(lastMonthStatistics.getTotal())) == 0 ? BigDecimal.ONE : lastMonthStatistics.getTotal();
        return thisMonthStatistics.getTotal().multiply(BigDecimal.valueOf(100)).divide(total, RoundingMode.HALF_UP).intValue();
    }

    private StatisticsCurrentMonthInfo generateStatisticsForRange(
            final LocalDateTime start,
            final LocalDateTime end
    ) {
        final List<CustomerAppointment> appointmentsInRange =
                this.customerAppointmentRepository.findAllByStartIsGreaterThanEqualAndStartLessThanEqual(
                        start,
                        end
                );

        BigDecimal activityRevenueSum = BigDecimal.ZERO;
        BigDecimal productRevenueSum = BigDecimal.ZERO;

        for (final CustomerAppointment currentCustomerAppointment : appointmentsInRange) {

            for (final CustomerAppointmentActivity activity : currentCustomerAppointment.getActivities()) {
                activityRevenueSum = activityRevenueSum.add(ActivityPriceUtils.getActivityPriceForCustomerAppointment(activity, currentCustomerAppointment));
            }

            for (final CustomerAppointmentSoldProduct soldProduct : currentCustomerAppointment.getSoldProducts()) {
                productRevenueSum = productRevenueSum.add(ProductPriceUtils.getSoldProductPriceForCustomerAppointment(soldProduct, currentCustomerAppointment));
            }

        }
        final BigDecimal totalAppointmentRevenueSum = activityRevenueSum.add(productRevenueSum);


        return StatisticsCurrentMonthInfo.builder()
                .name(end.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN))
                .activities(activityRevenueSum)
                .products(productRevenueSum)
                .total(totalAppointmentRevenueSum)
                .build();
    }

}
