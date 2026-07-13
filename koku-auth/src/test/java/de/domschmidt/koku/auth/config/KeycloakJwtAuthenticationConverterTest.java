package de.domschmidt.koku.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtAuthenticationConverterTest {

    private final KeycloakJwtAuthenticationConverter converter = new KeycloakJwtAuthenticationConverter();

    @Test
    void combinesScopesAndKokuResourceRoles() {
        final Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("scope", "openid profile")
                .claim("resource_access", Map.of("koku", Map.of("roles", List.of("calendar-admin", "user", 42))))
                .build();

        assertThat(converter.convert(jwt).getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("SCOPE_openid", "SCOPE_profile", "ROLE_calendar_admin", "ROLE_user");
    }

    @Test
    void malformedOrForeignResourceAccessAddsNoRoles() {
        final Jwt missing = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject")
                .build();
        final Jwt foreign = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject")
                .claim("resource_access", Map.of("other", Map.of("roles", List.of("admin"))))
                .build();
        final Jwt malformed = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("subject")
                .claim("resource_access", Map.of("koku", Map.of("roles", "admin")))
                .build();

        assertThat(converter.convert(missing).getAuthorities()).isEmpty();
        assertThat(converter.convert(foreign).getAuthorities()).isEmpty();
        assertThat(converter.convert(malformed).getAuthorities()).isEmpty();
    }
}
