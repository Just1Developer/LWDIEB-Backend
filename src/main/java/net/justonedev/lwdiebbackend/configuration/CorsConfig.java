/* (C)2025 */
package net.justonedev.lwdiebbackend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Class that configures the CORS-header.
 */
@Configuration
public class CorsConfig {
	@Value("dashboard.variable.frontend-url")
	private String frontendUrl;

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NonNull CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins(frontendUrl).allowedMethods("GET", "POST", "PUT", "DELETE").allowCredentials(true);
				registry.addMapping("/ws-post").allowedOrigins(frontendUrl).allowedMethods("POST").allowCredentials(true);
			}
		};
	}
}
