package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.auth.KokuUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface KokuUserRepository extends JpaRepository<KokuUser, Long> {

    @Transactional(readOnly = true)
    Optional<KokuUser> findByUsernameEqualsIgnoreCase(String username);

    @Transactional(readOnly = true)
    List<KokuUser> findAllByDeletedIsFalseAndUsernameContainingIgnoreCaseOrUserDetails_FirstnameContainingIgnoreCaseOrUserDetails_LastnameContainingIgnoreCaseOrderByUserDetails_FirstnameAsc(String userNameSearch, String firstNameSearch, String lastNameSearch);

}
