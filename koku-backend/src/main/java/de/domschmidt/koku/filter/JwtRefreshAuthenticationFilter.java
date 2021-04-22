package de.domschmidt.koku.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.domschmidt.koku.configuration.AuthConfiguration;
import de.domschmidt.koku.dto.auth.LoginAttemptResponseDto;
import de.domschmidt.koku.persistence.dao.KokuUserRefreshTokenRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUserRefreshToken;
import de.domschmidt.koku.utils.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class JwtRefreshAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthConfiguration authConfiguration;
    private final KokuUserRefreshTokenRepository kokuUserRefreshTokenRepository;
    private final UserDetailsService userDetailsService;
    private Key signingKey;

    public JwtRefreshAuthenticationFilter(final UserDetailsService userDetailsService,
                                          final KokuUserRefreshTokenRepository kokuUserRefreshTokenRepository,
                                          final AuthConfiguration jwtConfig) {
        this.userDetailsService = userDetailsService;
        this.authConfiguration = jwtConfig;
        this.kokuUserRefreshTokenRepository = kokuUserRefreshTokenRepository;
        this.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(AuthEndpoints.REFRESH_ENDPOINT, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response) {
        final String jwtRefreshTokenValue = JwtUtils.extractJwtValueFromRequest(request, JwtUtils.JWTR_COOKIE_NAME);

        if (jwtRefreshTokenValue != null) {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwtRefreshTokenValue)
                    .getBody();

            final String refreshTokenId = claims.getId();
            if (refreshTokenId != null) {
                final KokuUserRefreshToken refreshToken = this.kokuUserRefreshTokenRepository.findByTokenId(refreshTokenId);
                if (refreshToken == null) {
                    throw new UsernameNotFoundException("Token has not been found");
                }
                this.kokuUserRefreshTokenRepository.save(refreshToken);

                final UserDetails userDetails =
                        this.userDetailsService.loadUserByUsername(refreshToken.getUser().getUsername());
                final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        refreshToken.getUser().getUsername(),
                        userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                return auth;
            } else {
                throw new UsernameNotFoundException("Username has not been found");
            }
        } else {
            throw new InvalidCookieException("Refresh token cookie not found");
        }
    }

    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain,
                                            final Authentication auth) throws IOException {
        final String refreshTokenId = UUID.randomUUID().toString();
        final LocalDateTime creationDate = LocalDateTime.now();
        final LocalDateTime accessTokenExpirationDate = LocalDateTime.now().plus(
                this.authConfiguration.getJwtTokenTTL(),
                ChronoUnit.SECONDS
        );

        final String refreshedAccessToken = JwtGenerator.generateAccessToken(
                refreshTokenId,
                creationDate,
                accessTokenExpirationDate,
                auth,
                getSigningKey(),
                SignatureAlgorithm.forName(this.authConfiguration.getJwtSignatureAlgorithm())
        );

        final Cookie jwtCookie = CookieUtils.generateCookie(
                JwtUtils.JWT_COOKIE_NAME,
                request.isSecure(),
                this.authConfiguration.getJwtTokenTTL(),
                refreshedAccessToken
        );

        final LoginAttemptResponseDto identityAuthResponseDto = generateAuthResponse(jwtCookie);

        response.addCookie(jwtCookie);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(new ObjectMapper().writeValueAsString(identityAuthResponseDto));
    }

    private Key getSigningKey() {
        if (this.signingKey == null) {
            this.signingKey = CryptoUtils.getPrivateKey(this.authConfiguration.getJwtSecret());
        }
        return this.signingKey;
    }

    private LoginAttemptResponseDto generateAuthResponse(final Cookie jwtCookie) {
        final LoginAttemptResponseDto loginAttemptResponseDto = new LoginAttemptResponseDto();
        loginAttemptResponseDto.setTokenTTL(jwtCookie.getMaxAge());
        return loginAttemptResponseDto;
    }
}