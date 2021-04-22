package de.domschmidt.koku.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class AuthConfiguration {

    @Value("${security.jwt.secret}")
    private String jwtSecret;
    @Value("${security.jwt.signatureAlgorithm}")
    private String jwtSignatureAlgorithm;
    @Value("${security.jwt.token.ttl}")
    private int jwtTokenTTL;
    @Value("${security.jwt.refreshToken.ttl}")
    private int jwtRefreshTokenTTL;

}
