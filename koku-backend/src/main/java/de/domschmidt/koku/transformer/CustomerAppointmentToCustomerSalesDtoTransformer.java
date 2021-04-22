package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.customer.CustomerSalesDto;
import de.domschmidt.koku.dto.product.ProductDto;
import de.domschmidt.koku.persistence.model.CustomerAppointment;
import de.domschmidt.koku.persistence.model.CustomerAppointmentSoldProduct;
import de.domschmidt.koku.transformer.common.ITransformer;

import java.util.ArrayList;
import java.util.List;

public class CustomerAppointmentToCustomerSalesDtoTransformer implements ITransformer<CustomerAppointment, CustomerSalesDto> {

    @Override
    public List<CustomerSalesDto> transformToDtoList(List<CustomerAppointment> modelList) {
        final List<CustomerSalesDto> result = new ArrayList<>();
        for (final CustomerAppointment customerAppointment : modelList) {
            final boolean appointmentHasSoldProducts = customerAppointment.getSoldProducts() != null
                    && !customerAppointment.getSoldProducts().isEmpty();
            if (appointmentHasSoldProducts) {
                result.add(transformToDto(customerAppointment));
            }
        }
        return result;
    }

    @Override
    public CustomerSalesDto transformToDto(final CustomerAppointment model) {
        final List<ProductDto> soldProducts = new ArrayList<>();
        for (final CustomerAppointmentSoldProduct soldProduct : model.getSoldProducts()) {
            soldProducts.add(new ProductToProductDtoTransformer().transformToDto(soldProduct.getProduct()));
        }
        return CustomerSalesDto.builder()
                .startDate(model.getStart().toLocalDate())
                .startTime(model.getStart().toLocalTime())
                // build a unique list of sold products.
                // treeset keeps the order
                .soldProducts(soldProducts)
                .build();
    }

    @Override
    public CustomerAppointment transformToEntity(CustomerSalesDto customerSalesDto) {
        return null;
    }
}
