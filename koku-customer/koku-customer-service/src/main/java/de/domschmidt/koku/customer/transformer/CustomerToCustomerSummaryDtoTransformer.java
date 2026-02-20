package de.domschmidt.koku.customer.transformer;

import de.domschmidt.koku.customer.persistence.Customer;
import de.domschmidt.koku.dto.customer.KokuCustomerSummaryDto;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class CustomerToCustomerSummaryDtoTransformer {

    public KokuCustomerSummaryDto transformToDto(final Customer model) {
        return KokuCustomerSummaryDto.builder()
                .id(model.getId())
                .fullName(Stream.of(model.getFirstname(), model.getLastname())
                                .filter(s -> s != null && !s.isEmpty())
                                .collect(Collectors.joining(", "))
                        + (model.isOnFirstnameBasis() ? " *" : ""))
                .build();
    }
}
