package de.domschmidt.koku.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.domschmidt.koku.dto.user.KokuUserRegionDto;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRegion;
import de.domschmidt.koku.user.persistence.UserRepository;
import de.domschmidt.koku.user.transformer.UserRegionToKokuUserRegionDtoTransformer;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

class UserRegionControllerTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserRegionToKokuUserRegionDtoTransformer transformer =
            mock(UserRegionToKokuUserRegionDtoTransformer.class);
    private final UserRegionController controller = new UserRegionController(repository, transformer);

    @Test
    void returnsConfiguredRegionAndFallsBackToEmptyRegion() {
        final User configured = new User("u-1");
        final UserRegion region = new UserRegion();
        configured.setRegion(region);
        final User unconfigured = new User("u-2");
        when(repository.findById("u-1")).thenReturn(Optional.of(configured));
        when(repository.findById("u-2")).thenReturn(Optional.of(unconfigured));
        when(transformer.transformToDto(region))
                .thenReturn(KokuUserRegionDto.builder().id(7L).build());
        when(transformer.transformToDto(org.mockito.ArgumentMatchers.argThat(value -> value != region)))
                .thenReturn(KokuUserRegionDto.builder().build());

        assertThat(controller.getMyRegionDetails(jwt("u-1")).getId()).isEqualTo(7L);
        assertThat(controller.getMyRegionDetails(jwt("u-2"))).isNotNull();
    }

    @Test
    void rejectsMissingSubjectAndUnknownUser() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        final Jwt unknownUser = jwt("missing");
        assertThatThrownBy(() -> controller.getMyRegionDetails(null)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.getMyRegionDetails(unknownUser))
                .isInstanceOf(ResponseStatusException.class);
    }

    private static Jwt jwt(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .build();
    }
}
