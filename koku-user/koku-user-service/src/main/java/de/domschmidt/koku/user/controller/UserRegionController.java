package de.domschmidt.koku.user.controller;

import de.domschmidt.koku.dto.user.KokuUserRegionDto;
import de.domschmidt.koku.user.persistence.User;
import de.domschmidt.koku.user.persistence.UserRegion;
import de.domschmidt.koku.user.persistence.UserRepository;
import de.domschmidt.koku.user.transformer.UserRegionToKokuUserRegionDtoTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserRegionController {

    private final UserRepository userRepository;
    private final UserRegionToKokuUserRegionDtoTransformer transformer;

    @GetMapping("/@self/region")
    public KokuUserRegionDto getMyRegionDetails(
            @AuthenticationPrincipal Jwt jwt
    ) {
        final User user = this.userRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        final UserRegion userRegion;
        if (user.getRegion() == null) {
            userRegion = new UserRegion();
        } else {
            userRegion = user.getRegion();
        }
        return this.transformer.transformToDto(userRegion);
    }

}
