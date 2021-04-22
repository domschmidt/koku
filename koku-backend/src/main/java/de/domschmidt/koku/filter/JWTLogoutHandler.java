package de.domschmidt.koku.filter;

import de.domschmidt.koku.configuration.AuthConfiguration;
import de.domschmidt.koku.persistence.dao.KokuUserRefreshTokenRepository;
import de.domschmidt.koku.persistence.model.auth.KokuUserRefreshToken;
import de.domschmidt.koku.utils.CookieUtils;
import de.domschmidt.koku.utils.CryptoUtils;
import de.domschmidt.koku.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;

import static de.domschmidt.koku.utils.CookieUtils.invalidateCookie;

@Component
public class JWTLogoutHandler implements LogoutSuccessHandler {

    private final KokuUserRefreshTokenRepository refreshTokenRepository;
    private final AuthConfiguration authConfiguration;

    private Key signingKey;

    @Autowired
    public JWTLogoutHandler(final KokuUserRefreshTokenRepository refreshTokenRepository,
                            final AuthConfiguration authConfiguration) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.authConfiguration = authConfiguration;
    }

    @Override
    public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) {
        final Cookie refreshTokenCookie = CookieUtils.findCookie(request.getCookies(), JwtUtils.JWTR_COOKIE_NAME);

        if (refreshTokenCookie != null) {
            final Claims claims = Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(refreshTokenCookie.getValue()).getBody();

            final String refreshTokenId = claims.getId();
            if (refreshTokenId != null) {
                final KokuUserRefreshToken refreshToken = refreshTokenRepository.findByTokenId(refreshTokenId);
                if (refreshToken != null) {
                    refreshTokenRepository.delete(refreshToken);
                }
            }
            response.addCookie(invalidateCookie(refreshTokenCookie));
        }

        invalidateAccessToken(request, response);

        response.setStatus(HttpServletResponse.SC_OK);
    }

    public void invalidateAccessToken(final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie accessTokenCookie = CookieUtils.findCookie(request.getCookies(), JwtUtils.JWT_COOKIE_NAME);
        if (accessTokenCookie != null) {
            response.addCookie(invalidateCookie(accessTokenCookie));
        }
    }

    private Key getSigningKey() {
        if (this.signingKey == null) {
            this.signingKey = CryptoUtils.getPrivateKey(this.authConfiguration.getJwtSecret());
        }
        return this.signingKey;
    }
}