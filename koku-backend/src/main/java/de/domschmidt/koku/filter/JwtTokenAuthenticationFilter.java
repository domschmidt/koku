package de.domschmidt.koku.filter;

import de.domschmidt.koku.configuration.AuthConfiguration;
import de.domschmidt.koku.utils.CryptoUtils;
import de.domschmidt.koku.utils.JwtClaims;
import de.domschmidt.koku.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthConfiguration authConfiguration;
    private Key signingKey;

    public JwtTokenAuthenticationFilter(final AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws ServletException, IOException {
        final String jwtValue = JwtUtils.extractJwtValueFromRequest(request, JwtUtils.JWT_COOKIE_NAME);

        if (jwtValue == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(jwtValue)
                    .getBody();

            final String userName = claims.getSubject();
            if (userName != null) {
                @SuppressWarnings("unchecked")
                List<String> authorities = (List<String>) claims.get(JwtClaims.AUTHORITIES);

                final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userName, null,
                        authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private Key getSigningKey() {
        if (this.signingKey == null) {
            this.signingKey = CryptoUtils.getPublicKey(this.authConfiguration.getJwtSecret());
        }
        return this.signingKey;
    }

}