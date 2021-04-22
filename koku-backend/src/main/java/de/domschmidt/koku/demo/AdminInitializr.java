package de.domschmidt.koku.demo;

import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.persistence.model.auth.KokuUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Slf4j
public class AdminInitializr {

    public static final KokuUser DEFAULT_ADMIN_USER_STUB = KokuUser.builder()
            .username("admin")
            .userDetails(KokuUserDetails.builder()
                    .firstname("Admin")
                    .lastname("Admin")
                    .avatarBase64("")
                    .build())
            .build();

    private final KokuUserRepository kokuUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AdminInitializr(final KokuUserRepository kokuUserRepository,
                           final BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.kokuUserRepository = kokuUserRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Creates the relevant users if not existing yet.
     */
    @PostConstruct
    @Transactional
    public void init() {
        final Optional<KokuUser> existingAdminUser = this.kokuUserRepository.findByUsernameEqualsIgnoreCase("admin");
        if (existingAdminUser.isEmpty()) {
            log.warn("Administrator account not created yet. Creating Admin account...");
            final KokuUser admin = DEFAULT_ADMIN_USER_STUB.toBuilder()
                    .password(this.bCryptPasswordEncoder.encode("admin"))
                    .build();
            admin.getUserDetails().setUser(admin);
            admin.setPassword(this.bCryptPasswordEncoder.encode("admin"));
            this.kokuUserRepository.save(admin);
        }
    }

}

