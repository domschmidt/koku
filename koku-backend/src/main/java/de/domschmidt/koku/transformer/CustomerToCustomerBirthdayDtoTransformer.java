package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.customer.CustomerBirthdayDto;
import de.domschmidt.koku.persistence.model.Customer;
import de.domschmidt.koku.transformer.common.ITransformer;

import java.util.ArrayList;
import java.util.List;

public class CustomerToCustomerBirthdayDtoTransformer implements ITransformer<Customer, CustomerBirthdayDto> {

    public List<CustomerBirthdayDto> transformToDtoList(final List<Customer> modelList) {
        final List<CustomerBirthdayDto> result = new ArrayList<>();
        for (final Customer customer : modelList) {
            result.add(transformToDto(customer));
        }
        return result;
    }

    public CustomerBirthdayDto transformToDto(final Customer model) {
        return CustomerBirthdayDto.builder()
                .birthday(model.getBirthday())
                .id(model.getId())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .build();
    }

    @Override
    public Customer transformToEntity(CustomerBirthdayDto customerBirthdayDto) {
        throw new UnsupportedOperationException();
    }

}
