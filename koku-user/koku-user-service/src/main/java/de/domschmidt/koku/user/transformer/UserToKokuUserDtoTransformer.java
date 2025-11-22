package de.domschmidt.koku.user.transformer;

import de.domschmidt.koku.dto.user.KokuUserDto;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRegion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class UserToKokuUserDtoTransformer {

    private final EntityManager entityManager;

    public KokuUserDto transformToDto(final User model) {
        return KokuUserDto.builder()
                .id(model.getId())
                .deleted(model.isDeleted())
                .version(model.getVersion())
                .firstname(model.getFirstname())
                .lastname(model.getLastname())
                .initials(
                        (model.getFirstname() != null ? StringUtils.truncate(model.getFirstname(), 1) : "") +
                                (model.getLastname() != null ? StringUtils.truncate(model.getLastname(), 1) : "")
                )
                .fullname(String.join(" ", Stream.of(model.getFirstname(), model.getLastname()).filter(s -> s != null && !s.isEmpty()).toList()))
                .fullname((
                        (model.getFirstname() != null ? model.getFirstname() : "")
                                + " "
                                + (model.getLastname() != null ? model.getLastname() : "")
                ).trim())
                .avatarBase64(model.getAvatarBase64())
                .regionId(model.getRegion() != null ? model.getRegion().getId() : null)
                .updated(model.getUpdated())
                .recorded(model.getRecorded())
                .build();
    }

    public User transformToEntity(
            final User model,
            final KokuUserDto updatedDto
    ) {
        if (updatedDto.getFirstname() != null) {
            model.setFirstname(updatedDto.getFirstname());
        }
        if (updatedDto.getLastname() != null) {
            model.setLastname(updatedDto.getLastname());
        }
        if (updatedDto.getAvatarBase64() != null) {
            model.setAvatarBase64(updatedDto.getAvatarBase64());
        }
        if (updatedDto.getRegionId() != null) {
            model.setRegion(this.entityManager.getReference(UserRegion.class, updatedDto.getRegionId()));
        } else {
            model.setRegion(null);
        }
        return model;
    }
}
