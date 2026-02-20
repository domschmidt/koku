package de.domschmidt.koku.carddav.config;

import de.domschmidt.koku.auth.config.KeycloakJwtAuthenticationConverter;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final String authUserName;
    private final String authUserPassword;

    @Autowired
    public SecurityConfig(
            @Value("${carddav.username}") String authUserName, @Value("${carddav.password}") String authUserPassword) {
        this.authUserName = authUserName;
        this.authUserPassword = authUserPassword;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public StrictHttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowedHttpMethods(
                Arrays.asList("HEAD", "DELETE", "POST", "GET", "OPTIONS", "PATCH", "PUT", "PROPFIND", "REPORT"));
        return firewall;
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(final PasswordEncoder passwordEncoder) {
        UserDetails user = User.withUsername(this.authUserName)
                .password(passwordEncoder.encode(this.authUserPassword))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
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
                                jwtConfigurer.jwtAuthenticationConverter(new KeycloakJwtAuthenticationConverter())))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
