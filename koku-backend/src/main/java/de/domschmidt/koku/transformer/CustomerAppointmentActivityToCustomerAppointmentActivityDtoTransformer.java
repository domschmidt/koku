package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.customer.CustomerAppointmentActivityDto;
import de.domschmidt.koku.persistence.model.CustomerAppointmentActivity;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerAppointmentActivityToCustomerAppointmentActivityDtoTransformer implements ITransformer<CustomerAppointmentActivity, CustomerAppointmentActivityDto> {

    @Override
    public List<CustomerAppointmentActivityDto> transformToDtoList(final List<CustomerAppointmentActivity> modelList) {
        final List<CustomerAppointmentActivityDto> result = new ArrayList<>();
        for (final CustomerAppointmentActivity soldProduct : modelList) {
            if (soldProduct != null) {
                result.add(transformToDto(soldProduct, false));
            }
        }
        return result;
    }

    @Override
    public CustomerAppointmentActivityDto transformToDto(final CustomerAppointmentActivity model) {
        return transformToDto(model, true);
    }

    public CustomerAppointmentActivityDto transformToDto(final CustomerAppointmentActivity model, final boolean detailed) {
        return CustomerAppointmentActivityDto.builder()
                .id(model.getId())
                .activity(new ActivityToActivityDtoTransformer().transformToDto(model.getActivity(), detailed))
                .sellPrice(model.getSellPrice())
                .build();
    }

    @Override
    public CustomerAppointmentActivity transformToEntity(final CustomerAppointmentActivityDto productDto) {
        return CustomerAppointmentActivity.builder()
                .id(productDto.getId())
                .activity(new ActivityToActivityDtoTransformer().transformToEntity(productDto.getActivity()))
                .build();
    }
}
