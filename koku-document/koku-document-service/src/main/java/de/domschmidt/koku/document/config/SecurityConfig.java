package de.domschmidt.koku.document.config;

import de.domschmidt.koku.auth.config.KeycloakJwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf((csrf) -> csrf.disable())
                .sessionManagement((sessionMgmt) -> sessionMgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(
                        (req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/error", "/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer ->
                        httpSecurityOAuth2ResourceServerConfigurer.jwt(jwtConfigurer ->
                                jwtConfigurer.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())));
        return http.build();
    }
}
