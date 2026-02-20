package de.domschmidt.koku.customer.domain;

import de.domschmidt.koku.customer.persistence.CustomerAppointmentActivity;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentActivityDto;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class KokuCustomerAppointmentActivityDomain {

    Long activityId;
    BigDecimal price;

    public static KokuCustomerAppointmentActivityDomain fromDto(final KokuCustomerAppointmentActivityDto dto) {
        return new KokuCustomerAppointmentActivityDomain(dto.getActivityId(), dto.getPrice());
    }

    public static KokuCustomerAppointmentActivityDomain fromEntity(final CustomerAppointmentActivity entity) {
        return new KokuCustomerAppointmentActivityDomain(entity.getActivityId(), entity.getSellPrice());
    }
}
