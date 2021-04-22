package de.domschmidt.koku.persistence.dao;

import de.domschmidt.koku.persistence.model.auth.KokuUserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface KokuUserRefreshTokenRepository extends JpaRepository<KokuUserRefreshToken, Long> {

    @Transactional
    KokuUserRefreshToken findByTokenId(String tokenId);

}
