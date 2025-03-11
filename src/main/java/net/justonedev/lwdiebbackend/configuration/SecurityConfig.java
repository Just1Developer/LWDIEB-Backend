/* (C)2025 */
package net.justonedev.lwdiebbackend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * This class configures security settings for the application. Defines
 * authentication rules and sets up OAuth2 (google) and logout behavior.
 */
@Configuration
public class SecurityConfig {

	/**
	 * Sets up the security filter chain with authentication rules, OAuth2
	 * JsonWebToken handling and custom logout behavior.
	 *
	 * @param http the {@link HttpSecurity} instance to configure with this methode
	 * @return the configured {@link SecurityFilterChain}
	 * @throws Exception if an error occurs during configuration process
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize.requestMatchers("/ws").permitAll().requestMatchers("/ws-post").permitAll());

		return http.build();
	}
}
