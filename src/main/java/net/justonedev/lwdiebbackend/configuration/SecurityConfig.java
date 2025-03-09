package net.justonedev.lwdiebbackend.configuration;

import de.dieb.dashboard.backend.auth.AppUserAuthenticationConverter;
import de.dieb.dashboard.backend.auth.CustomEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * This class configures security settings for the application.
 * Defines authentication rules and sets up OAuth2 (google) and logout behavior.
 */
@Configuration
public class SecurityConfig {

    /**
     * Sets up the security filter chain with authentication rules,
     * OAuth2 JsonWebToken handling and custom logout behavior.
     *
     * @param http the {@link HttpSecurity} instance to configure with this methode
     * @param appUserAuthenticationConverter custom JWT authentication converter
     * @param customEntryPoint sets the custom entry point and implements {@link AuthenticationEntryPoint}
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration process
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AppUserAuthenticationConverter appUserAuthenticationConverter,
            CustomEntryPoint customEntryPoint)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/api/widgets/**")
                                        .permitAll()
                                        .requestMatchers("/api/dashboard")
                                        .permitAll()
                                        .requestMatchers("/ws")
                                        .permitAll()
                                        .requestMatchers("/logout")
                                        .permitAll()
                                        .requestMatchers("/auth/login/kit")
                                        .permitAll()
                                        .requestMatchers("/auth/login/google")
                                        .permitAll()
                                        .requestMatchers("/auth/callback")
                                        .permitAll()
                                        .requestMatchers("/auth/refresh")
                                        .permitAll()
                                        .requestMatchers("/auth/**")
                                        .authenticated()
                                        .requestMatchers("/api/user")
                                        .authenticated()
                                        .requestMatchers("/api/user/theme")
                                        .authenticated()
                                        .requestMatchers(
                                                new AntPathRequestMatcher(
                                                        "/api/global-dashboard", "POST"))
                                        .authenticated()
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                                jwtConfigurer ->
                                                        jwtConfigurer.jwtAuthenticationConverter(
                                                                appUserAuthenticationConverter))
                                        .authenticationEntryPoint(customEntryPoint))
                .logout(
                        logout ->
                                logout.logoutSuccessUrl("/")
                                        .invalidateHttpSession(true)
                                        .clearAuthentication(true)
                                        .deleteCookies("JSESSIONID", "access_token"));

        return http.build();
    }
}
