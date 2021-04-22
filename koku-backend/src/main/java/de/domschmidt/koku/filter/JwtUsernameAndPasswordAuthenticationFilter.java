package de.domschmidt.koku.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.domschmidt.koku.configuration.AuthConfiguration;
import de.domschmidt.koku.dto.auth.LoginAttemptResponseDto;
import de.domschmidt.koku.dto.auth.LoginDto;
import de.domschmidt.koku.persistence.dao.KokuUserRefreshTokenRepository;
import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.persistence.model.auth.KokuUserRefreshToken;
import de.domschmidt.koku.utils.*;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authManager;

    private final AuthConfiguration authConfiguration;
    private final KokuUserRefreshTokenRepository refreshTokenRepository;
    private final KokuUserRepository userRepository;
    private Key signingKey;

    public JwtUsernameAndPasswordAuthenticationFilter(final AuthenticationManager authManager,
                                                      final KokuUserRefreshTokenRepository refreshTokenRepository,
                                                      final KokuUserRepository userRepository,
                                                      final AuthConfiguration authConfiguration) {
        this.authManager = authManager;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authConfiguration = authConfiguration;
        this.userRepository = userRepository;

        setRequiresAuthenticationRequestMatcher(
                new AntPathRequestMatcher(AuthEndpoints.LOGIN_ENDPOINT, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final LoginDto authRequest = new ObjectMapper().readValue(request.getInputStream(), LoginDto.class);
            final UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
                    authRequest.getPassword(), Collections.emptyList());
            return authManager.authenticate(authToken);
        } catch (final IOException e) {
            throw new BadCredentialsException("Unable to parse given Input", e);
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain,
                                            final Authentication auth) throws IOException {
        final Optional<KokuUser> kokuUser = this.userRepository.findByUsernameEqualsIgnoreCase(auth.getName());

        if (kokuUser.isPresent()) {
            final String accessTokenId = UUID.randomUUID().toString();

            final LocalDateTime creationDate = LocalDateTime.now();
            final LocalDateTime accessTokenExpirationDate = creationDate.plus(this.authConfiguration.getJwtTokenTTL(), ChronoUnit.SECONDS);
            final LocalDateTime refreshTokenExpirationDate = creationDate.plus(this.authConfiguration.getJwtRefreshTokenTTL(), ChronoUnit.SECONDS);

            final String accessToken = JwtGenerator.generateAccessToken(accessTokenId, creationDate, accessTokenExpirationDate, auth,
                    getSigningKey(),
                    SignatureAlgorithm.forName(this.authConfiguration.getJwtSignatureAlgorithm()));
            final String refreshTokenId = UUID.randomUUID().toString();
            final String refreshToken = JwtGenerator.generateRefreshToken(refreshTokenId, creationDate, refreshTokenExpirationDate, auth,
                    getSigningKey(),
                    SignatureAlgorithm.forName(this.authConfiguration.getJwtSignatureAlgorithm()));

            final KokuUserRefreshToken refreshTokenEntity = new KokuUserRefreshToken();
            refreshTokenEntity.setTokenId(refreshTokenId);
            refreshTokenEntity.setUser(kokuUser.get());
            refreshTokenEntity.setExpires(refreshTokenExpirationDate);
            refreshTokenRepository.save(refreshTokenEntity);

            final Cookie accessTokenCookie = CookieUtils.generateCookie(JwtUtils.JWT_COOKIE_NAME, request.isSecure(), this.authConfiguration.getJwtTokenTTL(),
                    accessToken);
            final Cookie refreshTokenCookie = CookieUtils.generateCookie(JwtUtils.JWTR_COOKIE_NAME, request.isSecure(),
                    this.authConfiguration.getJwtRefreshTokenTTL(), refreshToken);

            final LoginAttemptResponseDto authResponseDto = generateAuthResponse(
                    this.authConfiguration.getJwtTokenTTL(),
                    this.authConfiguration.getJwtRefreshTokenTTL()
            );

            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(new ObjectMapper().writeValueAsString(authResponseDto));
        } else {
            throw new UsernameNotFoundException("Username not found.");
        }
    }

    private Key getSigningKey() {
        if (this.signingKey == null) {
            this.signingKey = CryptoUtils.getPrivateKey(this.authConfiguration.getJwtSecret());
        }
        return this.signingKey;
    }

    private LoginAttemptResponseDto generateAuthResponse(final long accessTokenTTL,
                                                         final long refreshTokenTTL
    ) {
        final LoginAttemptResponseDto authResponseDto = new LoginAttemptResponseDto();
        authResponseDto.setTokenTTL(accessTokenTTL);
        authResponseDto.setRefreshTokenTTL(refreshTokenTTL);
        return authResponseDto;
    }

}