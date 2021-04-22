package de.domschmidt.koku.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class JwtGenerator {

    private JwtGenerator() {
    }

    public static String generateAccessToken(final String id,
                                             final LocalDateTime creationDate,
                                             final LocalDateTime expiryDate,
                                             final Authentication auth,
                                             final Key signingKey,
                                             final SignatureAlgorithm signatureAlgorithm) {
        return generateJwt(
                id,
                creationDate,
                expiryDate,
                auth,
                signingKey,
                signatureAlgorithm,
                new ArrayList<>()
        ).compact();
    }

    public static String generateRefreshToken(final String id,
                                              final LocalDateTime creationDate,
                                              final LocalDateTime expiryDate,
                                              final Authentication auth,
                                              final Key signingKey,
                                              final SignatureAlgorithm signatureAlgorithm) {
        final List<String> scopes = new ArrayList<>();
        scopes.add(JwtRoles.REFRESH_TOKEN);
        return generateJwt(id, creationDate, expiryDate, auth, signingKey, signatureAlgorithm, scopes).compact();
    }

    private static JwtBuilder generateJwt(final String id, final LocalDateTime creationDate, final LocalDateTime expiryDate,
                                          final Authentication auth, final Key signingKey,
                                          final SignatureAlgorithm signatureAlgorithm, final List<String> scopes) {
        final long negativeOffsetSeconds = 60;
        final LocalDateTime notBeforeDate = creationDate.minus(negativeOffsetSeconds, ChronoUnit.SECONDS);
        return Jwts.builder().setSubject(auth.getName())
                .claim(JwtClaims.AUTHORITIES, auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .claim(JwtClaims.SCOPES, scopes)
                .setIssuedAt(Date.from(creationDate.atZone(ZoneId.systemDefault()).toInstant()))
                .setNotBefore(Date.from(notBeforeDate.atZone(ZoneId.systemDefault()).toInstant()))
                .setId(id)
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(signingKey, signatureAlgorithm);
    }
}