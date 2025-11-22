package de.domschmidt.koku.customer.kafka.customers.transformer;

import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentActivityKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentPromotionKafkaDto;
import de.domschmidt.koku.customer.kafka.dto.CustomerAppointmentSoldProductKafkaDto;
import de.domschmidt.koku.customer.persistence.CustomerAppointment;

import java.util.ArrayList;

public class CustomerAppointmentToKafkaCustomerAppointmentDtoTransformer {

    public CustomerAppointmentKafkaDto transformToDto(final CustomerAppointment model) {
        return CustomerAppointmentKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .start(model.getStart())
                .description(model.getDescription())
                .additionalInfo(model.getAdditionalInfo())
                .customerId(model.getCustomer().getId())
                .userId(model.getUserId())
                .activities(model.getActivities() != null ? model.getActivities().stream().map(customerAppointmentActivity -> CustomerAppointmentActivityKafkaDto.builder()
                        .activityId(customerAppointmentActivity.getActivityId())
                        .sellPrice(customerAppointmentActivity.getSellPrice())
                        .build()
                ).toList() : new ArrayList<>())
                .promotions(model.getPromotions() != null ? model.getPromotions().stream().map(customerAppointmentPromotion -> CustomerAppointmentPromotionKafkaDto.builder()
                        .promotionId(customerAppointmentPromotion.getPromotionId())
                        .build()
                ).toList() : new ArrayList<>())
                .soldProducts(model.getSoldProducts() != null ? model.getSoldProducts().stream().map(customerAppointmentSoldProduct -> CustomerAppointmentSoldProductKafkaDto.builder()
                        .productId(customerAppointmentSoldProduct.getProductId())
                        .sellPrice(customerAppointmentSoldProduct.getSellPrice())
                        .build()
                ).toList() : new ArrayList<>())

                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
