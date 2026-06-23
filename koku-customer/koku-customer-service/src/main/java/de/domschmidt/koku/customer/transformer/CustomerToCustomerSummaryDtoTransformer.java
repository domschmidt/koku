package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.dto.customer.KokuCustomerSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class CustomerToCustomerSummaryDtoTransformer {

    public KokuCustomerSummaryDto transformToDto(final Customer model) {
        return KokuCustomerSummaryDto.builder()
                .id(model.getId())
                .fullName(CustomerNameFormatter.displayName(model))
                .build();
    }
}
