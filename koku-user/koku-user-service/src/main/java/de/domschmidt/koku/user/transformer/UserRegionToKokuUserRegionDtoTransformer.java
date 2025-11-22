package de.domschmidt.koku.user.transformer;

import de.domschmidt.koku.dto.user.KokuUserRegionDto;
import de.domschmidt.koku.user.persistence.UserRegion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegionToKokuUserRegionDtoTransformer {

    public KokuUserRegionDto transformToDto(final UserRegion model) {
        return KokuUserRegionDto.builder()
                .id(model.getId())
                .country(model.getCountryIso())
                .state(model.getStateIso())
                .build();
    }

}
