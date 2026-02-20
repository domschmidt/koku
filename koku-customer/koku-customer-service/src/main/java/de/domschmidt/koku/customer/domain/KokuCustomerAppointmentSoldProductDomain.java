package de.domschmidt.koku.customer.domain;

import de.domschmidt.koku.customer.persistence.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.dto.customer.KokuCustomerAppointmentSoldProductDto;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class KokuCustomerAppointmentSoldProductDomain {

    Long productId;
    BigDecimal price;

    public static KokuCustomerAppointmentSoldProductDomain fromDto(final KokuCustomerAppointmentSoldProductDto dto) {
        return new KokuCustomerAppointmentSoldProductDomain(dto.getProductId(), dto.getPrice());
    }

    public static KokuCustomerAppointmentSoldProductDomain fromEntity(final CustomerAppointmentSoldProduct entity) {
        return new KokuCustomerAppointmentSoldProductDomain(entity.getProductId(), entity.getSellPrice());
    }
}
