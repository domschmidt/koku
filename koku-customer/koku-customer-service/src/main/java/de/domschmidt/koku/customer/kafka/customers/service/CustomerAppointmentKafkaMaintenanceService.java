package de.domschmidt.koku.customer.kafka.customers.service;

import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentActivityDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentPromotionDomain;
import de.domschmidt.koku.customer.domain.KokuCustomerAppointmentSoldProductDomain;
import de.domschmidt.koku.customer.kafka.KafkaStreamsRunningEvent;
import de.domschmidt.koku.customer.kafka.activities.service.ActivityKTableProcessor;
import de.domschmidt.koku.customer.kafka.activity_steps.service.ActivityStepKTableProcessor;
import de.domschmidt.koku.customer.kafka.products.service.ProductKTableProcessor;
import de.domschmidt.koku.customer.kafka.promotions.service.PromotionKTableProcessor;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentActivity;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentRepository;
import de.domschmidt.koku.customer.persistence.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.customer.transformer.CustomerAppointmentToCustomerAppointmentDtoTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@ConditionalOnBooleanProperty("koku.maintenance")
@RequiredArgsConstructor
public class CustomerAppointmentKafkaMaintenanceService implements ApplicationListener<KafkaStreamsRunningEvent> {

    final CustomerAppointmentRepository customerAppointmentRepository;
    final CustomerAppointmentKafkaService customerAppointmentKafkaService;
    final CustomerAppointmentToCustomerAppointmentDtoTransformer transformer;

    final ActivityKTableProcessor activityKTableProcessor;
    final ProductKTableProcessor productKTableProcessor;
    final ActivityStepKTableProcessor activityStepKTableProcessor;
    final PromotionKTableProcessor promotionKTableProcessor;

    @Override
    @Transactional
    public void onApplicationEvent(KafkaStreamsRunningEvent event) {
        this.customerAppointmentRepository.findAll().forEach(model -> {
            final List<KokuCustomerAppointmentPromotionDomain> allPromotions =
                    model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList();

            model.setSoldProductsRevenueSnapshot(this.transformer.calculateCustomerAppointmentSoldProductPriceSum(
                    model.getStart(),
                    model.getSoldProducts().stream().map(KokuCustomerAppointmentSoldProductDomain::fromEntity).toList(),
                    model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
            ));
            model.setActivitiesRevenueSnapshot(this.transformer.calculateCustomerAppointmentActivityPriceSum(
                    model.getStart(),
                    model.getActivities().stream().map(KokuCustomerAppointmentActivityDomain::fromEntity).toList(),
                    model.getPromotions().stream().map(KokuCustomerAppointmentPromotionDomain::fromEntity).toList()
            ));

            for (final CustomerAppointmentSoldProduct soldProduct : model.getSoldProducts()) {
                soldProduct.setFinalPriceSnapshot(this.transformer.calculateSoldProductPrice(
                        model.getStart(),
                        KokuCustomerAppointmentSoldProductDomain.fromEntity(soldProduct),
                        allPromotions
                ));
            }

            for (final CustomerAppointmentActivity activity : model.getActivities()) {
                activity.setFinalPriceSnapshot(this.transformer.calculateActivityPrice(
                        model.getStart(),
                        KokuCustomerAppointmentActivityDomain.fromEntity(activity),
                        allPromotions
                ));
            }

            try {
                this.customerAppointmentKafkaService.sendCustomerAppointment(model);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.error("Error sending customer appointment", e);
            }
        });
    }
}
