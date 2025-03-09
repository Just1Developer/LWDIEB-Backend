package net.justonedev.lwdiebbackend.configuration;

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
    @Bean
    public WebMvcConfigurer corsConfigurer(VariableProperties variableProperties) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(variableProperties.getFrontendUrl(), "pse-next")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true);
            }
        };
    }
}
