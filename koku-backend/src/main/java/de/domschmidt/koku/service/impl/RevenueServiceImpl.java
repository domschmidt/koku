package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.dto.statistic.StatisticsCurrentMonthInfo;
import de.domschmidt.koku.persistence.dao.CustomerAppointmentRepository;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.service.IRevenueService;
import de.domschmidt.koku.utils.ActivityPriceUtils;
import de.domschmidt.koku.utils.ProductPriceUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
public class RevenueServiceImpl implements IRevenueService {

    private final CustomerAppointmentRepository customerAppointmentRepository;

    public RevenueServiceImpl(final CustomerAppointmentRepository customerAppointmentRepository) {
        this.customerAppointmentRepository = customerAppointmentRepository;
    }

    public StatisticsCurrentMonthInfo generateStatisticsForRange(
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
