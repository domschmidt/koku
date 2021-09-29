package de.domschmidt.koku.configuration;

import de.domschmidt.koku.filter.JWTLogoutHandler;
import de.domschmidt.koku.filter.JwtRefreshAuthenticationFilter;
import de.domschmidt.koku.filter.JwtTokenAuthenticationFilter;
import de.domschmidt.koku.filter.JwtUsernameAndPasswordAuthenticationFilter;
import de.domschmidt.koku.persistence.dao.KokuUserRefreshTokenRepository;
import de.domschmidt.koku.persistence.dao.KokuUserRepository;
import de.domschmidt.koku.service.impl.UserDetailsService;
import de.domschmidt.koku.utils.AuthEndpoints;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final KokuUserRefreshTokenRepository refreshTokenRepository;
    private final AuthConfiguration authConfiguration;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JWTLogoutHandler logoutHandler;
    private final KokuUserRepository userRepository;

    public SecurityConfiguration(final UserDetailsService userDetailsService,
                                 final BCryptPasswordEncoder bCryptPasswordEncoder,
                                 final AuthConfiguration authConfiguration,
                                 final KokuUserRefreshTokenRepository refreshTokenRepository,
                                 final KokuUserRepository userRepository,
                                 final JWTLogoutHandler logoutHandler
    ) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authConfiguration = authConfiguration;
        this.userRepository = userRepository;
        this.logoutHandler = logoutHandler;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)).and()
                .addFilterAfter(new JwtTokenAuthenticationFilter(this.authConfiguration), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), this.refreshTokenRepository, this.userRepository, this.authConfiguration))
                .addFilter(new JwtRefreshAuthenticationFilter(userDetailsService(), this.refreshTokenRepository, this.authConfiguration))
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, AuthEndpoints.REFRESH_ENDPOINT, AuthEndpoints.LOGIN_ENDPOINT, AuthEndpoints.LOGOUT_ENDPOINT).permitAll()
                .anyRequest().authenticated()
                .and()
                .logout().logoutRequestMatcher(new AntPathRequestMatcher(AuthEndpoints.LOGOUT_ENDPOINT)).logoutSuccessHandler(logoutHandler);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

}
