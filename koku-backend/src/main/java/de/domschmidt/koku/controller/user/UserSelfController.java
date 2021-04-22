package de.domschmidt.koku.controller.user;

import de.domschmidt.koku.dto.user.KokuUserDetailsDto;
import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.Optional;

@RestController
@RequestMapping("/users/@self")
public class UserSelfController {

    private final KokuUserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserSelfController(final KokuUserRepository userRepository,
                              final BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @GetMapping
    @Transactional
    public KokuUserDetailsDto getMyDetails() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Optional<KokuUser> kokuUserOptional = this.userRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        if (kokuUserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            final KokuUser kokuUser = kokuUserOptional.get();
            return KokuUserDetailsDto.builder()
                    .id(kokuUser.getId())
                    .username(kokuUser.getUsername())
                    .firstname(kokuUser.getUserDetails().getFirstname())
                    .lastname(kokuUser.getUserDetails().getLastname())
                    .avatarBase64(kokuUser.getUserDetails().getAvatarBase64())
                    .build();
        }
    }

    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    @Transactional
    public void updateMyDetails(@RequestBody KokuUserDetailsDto updatedDto) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Optional<KokuUser> kokuUserOptional = this.userRepository.findByUsernameEqualsIgnoreCase(authentication.getName());
        if (kokuUserOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } else {
            final KokuUser kokuUser = kokuUserOptional.get();
            kokuUser.setUsername(updatedDto.getUsername());
            kokuUser.getUserDetails().setFirstname(updatedDto.getFirstname());
            kokuUser.getUserDetails().setLastname(updatedDto.getLastname());
            kokuUser.getUserDetails().setAvatarBase64(updatedDto.getAvatarBase64());
            if (StringUtils.isNotEmpty(updatedDto.getPassword())) {
                kokuUser.setPassword(this.bCryptPasswordEncoder.encode(updatedDto.getPassword()));
            }
            this.userRepository.save(kokuUser);
        }
    }

}
