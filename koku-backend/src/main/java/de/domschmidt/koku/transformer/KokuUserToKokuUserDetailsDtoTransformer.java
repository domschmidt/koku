package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.user.KokuUserDetailsDto;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.persistence.model.auth.KokuUserDetails;
import de.domschmidt.koku.transformer.common.ITransformer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KokuUserToKokuUserDetailsDtoTransformer implements ITransformer<KokuUser, KokuUserDetailsDto> {

    @Override
    public List<KokuUserDetailsDto> transformToDtoList(final List<KokuUser> modelList) {
        final List<KokuUserDetailsDto> result = new ArrayList<>();
        for (final KokuUser user : modelList) {
            result.add(transformToDto(user));
        }
        return result;
    }

    @Override
    public KokuUserDetailsDto transformToDto(final KokuUser model) {
        return KokuUserDetailsDto.builder()
                .avatarBase64(model.getUserDetails().getAvatarBase64())
                .firstname(model.getUserDetails().getFirstname())
                .lastname(model.getUserDetails().getLastname())
                .username(model.getUsername())
                .id(model.getId())
                .build();
    }

    @Override
    public KokuUser transformToEntity(final KokuUserDetailsDto kokuUserDetailsDto) {
        return KokuUser.builder()
                .userDetails(KokuUserDetails.builder()
                        .avatarBase64(kokuUserDetailsDto.getAvatarBase64())
                        .firstname(kokuUserDetailsDto.getFirstname())
                        .lastname(kokuUserDetailsDto.getLastname())
                        .build()
                )
                .username(kokuUserDetailsDto.getUsername())
                .id(kokuUserDetailsDto.getId())
                .password(kokuUserDetailsDto.getPassword() != null ? kokuUserDetailsDto.getPassword() : null)
                .build();
    }
}
