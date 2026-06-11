package de.domschmidt.koku.user.kafka.users.transformer;

import de.domschmidt.koku.user.kafka.dto.UserAppointmentKafkaDto;
import de.domschmidt.koku.user.persistence.UserAppointment;

public class UserAppointmentToKafkaUserAppointmentDtoTransformer {

    public UserAppointmentKafkaDto transformToDto(final UserAppointment model) {
        return UserAppointmentKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .start(model.getStartTimestamp())
                .end(model.getEndTimestamp())
                .description(model.getDescription())
                .userId(model.getUser() != null ? model.getUser().getId() : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
