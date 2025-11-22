package de.domschmidt.koku.user.kafka.users.transformer;

import de.domschmidt.koku.user.kafka.dto.UserKafkaDto;
import de.domschmidt.koku.user.persistence.User;

public class UserToKafkaUserDtoTransformer {

    public UserKafkaDto transformToDto(final User model) {
        return UserKafkaDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .firstname(model.getFirstname())
                .lastname(model.getLastname())
                .avatarBase64(model.getAvatarBase64())
                .countryIso(model.getRegion() != null ? model.getRegion().getCountryIso() : null)
                .stateIso(model.getRegion() != null ? model.getRegion().getStateIso() : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }
}
