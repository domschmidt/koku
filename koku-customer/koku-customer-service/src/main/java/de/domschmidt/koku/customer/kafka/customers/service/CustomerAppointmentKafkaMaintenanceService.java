package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentActivityDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentPromotionDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentSoldProductDomain;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentActivity;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentRepository;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerAppointmentKafkaMaintenanceService {

    final CustomerAppointmentRepository customerAppointmentRepository;
    final CustomerAppointmentKafkaService customerAppointmentKafkaService;
    final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer;

    @Transactional
    public void runMaintenance() {
        log.warn("###### MAINTENANCE ###### SEND CUSTOMER APPOINTMENTS ######");
        this.customerAppointmentRepository.findAll().forEach(model -> {
            final List<KokuCustomerAppointmentPromotionDomain> allPromotions = model.getPromotions().stream()
                    .map(KokuCustomerAppointmentPromotionDomain::fromEntity)
                    .toList();

            model.setSoldProductsRevenueSnapshot(this.transformer.calculateCustomerAppointmentSoldProductPriceSum(
                    model.getStart(),
                    model.getSoldProducts().stream()
                            .map(KokuCustomerAppointmentSoldProductDomain::fromEntity)
                            .toList(),
                    model.getPromotions().stream()
                            .map(KokuCustomerAppointmentPromotionDomain::fromEntity)
                            .toList()));
            model.setSoldProductsSummarySnapshot(
                    this.transformer.calculateCustomerAppointmentSoldProductSummary(model.getSoldProducts().stream()
                            .map(KokuCustomerAppointmentSoldProductDomain::fromEntity)
                            .toList()));
            model.setActivitiesRevenueSnapshot(this.transformer.calculateCustomerAppointmentActivityPriceSum(
                    model.getStart(),
                    model.getActivities().stream()
                            .map(KokuCustomerAppointmentActivityDomain::fromEntity)
                            .toList(),
                    model.getPromotions().stream()
                            .map(KokuCustomerAppointmentPromotionDomain::fromEntity)
                            .toList()));
            model.setActivitiesSummarySnapshot(
                    this.transformer.calculateCustomerAppointmentActivitySummary(model.getActivities().stream()
                            .map(KokuCustomerAppointmentActivityDomain::fromEntity)
                            .toList()));
            model.setCalculatedEndSnapshot(this.transformer.calculateCustomerAppointmentEnd(
                    model.getStart(),
                    model.getActivities().stream()
                            .map(KokuCustomerAppointmentActivityDomain::fromEntity)
                            .toList()));

            for (final CustomerAppointmentSoldProduct soldProduct : model.getSoldProducts()) {
                soldProduct.setFinalPriceSnapshot(this.transformer.calculateSoldProductPrice(
                        model.getStart(),
                        KokuCustomerAppointmentSoldProductDomain.fromEntity(soldProduct),
                        allPromotions));
            }

            for (final CustomerAppointmentActivity activity : model.getActivities()) {
                activity.setFinalPriceSnapshot(this.transformer.calculateActivityPrice(
                        model.getStart(), KokuCustomerAppointmentActivityDomain.fromEntity(activity), allPromotions));
            }

            try {
                this.customerAppointmentKafkaService.sendCustomerAppointment(model);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending customer appointment", e);
            }
        });
    }
}
