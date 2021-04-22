package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.auth.KokuUserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KokuUserDetailsRepository extends JpaRepository<KokuUserDetails, Long> {

}
