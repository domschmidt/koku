package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.customer.CustomerAppointmentSoldProductDto;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerAppointmentSoldProductToCustomerAppointmentSoldProductDtoTransformer implements ITransformer<CustomerAppointmentSoldProduct, CustomerAppointmentSoldProductDto> {

    @Override
    public List<CustomerAppointmentSoldProductDto> transformToDtoList(final List<CustomerAppointmentSoldProduct> modelList) {
        final List<CustomerAppointmentSoldProductDto> result = new ArrayList<>();
        for (final CustomerAppointmentSoldProduct soldProduct : modelList) {
            if (soldProduct != null) {
                result.add(transformToDto(soldProduct, false));
            }
        }
        return result;
    }

    @Override
    public CustomerAppointmentSoldProductDto transformToDto(final CustomerAppointmentSoldProduct model) {
        return transformToDto(model, true);
    }

    public CustomerAppointmentSoldProductDto transformToDto(final CustomerAppointmentSoldProduct model, final boolean detailed) {
        return CustomerAppointmentSoldProductDto.builder()
                .id(model.getId())
                .product(new ProductToProductDtoTransformer().transformToDto(model.getProduct(), detailed))
                .sellPrice(model.getSellPrice())
                .build();
    }

    @Override
    public CustomerAppointmentSoldProduct transformToEntity(final CustomerAppointmentSoldProductDto productDto) {
        return CustomerAppointmentSoldProduct.builder()
                .id(productDto.getId())
                .product(new ProductToProductDtoTransformer().transformToEntity(productDto.getProduct()))
                .build();
    }
}
