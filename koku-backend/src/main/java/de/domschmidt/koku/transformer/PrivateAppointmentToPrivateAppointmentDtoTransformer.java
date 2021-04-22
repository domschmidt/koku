package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.user.PrivateAppointmentDto;
import de.domschmidt.koku.persistence.model.PrivateAppointment;
import de.domschmidt.koku.transformer.common.ITransformer;

import java.util.ArrayList;
import java.util.List;

public class PrivateAppointmentToPrivateAppointmentDtoTransformer implements ITransformer<PrivateAppointment, PrivateAppointmentDto> {

    public List<PrivateAppointmentDto> transformToDtoList(final List<PrivateAppointment> modelList) {
        final List<PrivateAppointmentDto> result = new ArrayList<>();
        for (final PrivateAppointment privateAppointment : modelList) {
            result.add(transformToDto(privateAppointment));
        }
        return result;
    }

    public PrivateAppointmentDto transformToDto(final PrivateAppointment model) {
        final PrivateAppointmentDto.PrivateAppointmentDtoBuilder result = PrivateAppointmentDto.builder()
                .id(model.getId())
                .startDate(model.getStart().toLocalDate())
                .startTime(model.getStart().toLocalTime())
                .description(model.getDescription());

        if (model.getEnding() != null) {
            result.endDate(model.getEnding().toLocalDate())
                  .endTime(model.getEnding().toLocalTime());
        }

        return result.build();
    }

    public PrivateAppointment transformToEntity(final PrivateAppointmentDto dtoModel) {
        return PrivateAppointment.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .start(dtoModel.getStartDate().atTime(dtoModel.getStartTime()))
                .ending(dtoModel.getEndDate().atTime(dtoModel.getEndTime()))
                .build();
    }

}
