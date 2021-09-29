package de.domschmidt.koku.service.impl;

import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    private final KokuUserRepository kokuUserRepository;

    @Autowired
    public UserDetailsService(final KokuUserRepository kokuUserRepository) {
        this.kokuUserRepository = kokuUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Optional<KokuUser> applicationKokuUserOptional = kokuUserRepository.findByUsernameEqualsIgnoreCase(username);
        if (applicationKokuUserOptional.isEmpty()) {
            throw new UsernameNotFoundException(username);
        } else {
            final KokuUser applicationKokuUser = applicationKokuUserOptional.get();
            return new User(applicationKokuUser.getUsername(), applicationKokuUser.getPassword(), Collections.emptyList());
        }
    }
}
