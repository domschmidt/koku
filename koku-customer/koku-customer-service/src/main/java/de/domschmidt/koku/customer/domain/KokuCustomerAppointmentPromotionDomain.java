package de.domschmidt.koku.customer.domain;

import de.domschmidt.koku.customer.persistence.CustomerAppointmentPromotion;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentPromotionDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class KokuCustomerAppointmentPromotionDomain {

    Long promotionId;

    public static KokuCustomerAppointmentPromotionDomain fromDto(final KokuCustomerAppointmentPromotionDto dto) {
        return new KokuCustomerAppointmentPromotionDomain(dto.getPromotionId());
    }

    public static KokuCustomerAppointmentPromotionDomain fromEntity(final CustomerAppointmentPromotion entity) {
        return new KokuCustomerAppointmentPromotionDomain(entity.getPromotionId());
    }
}
