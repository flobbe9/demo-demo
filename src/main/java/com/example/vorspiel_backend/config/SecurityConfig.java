package com.example.vorspiel_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * Configuration class to authentiacate requests.
 * 
 * @since 0.0.1
 */
@Configuration
@EnableMethodSecurity // for @PreAuthorize
@EnableWebSecurity
public class SecurityConfig {

    @Value("${FRONTEND_BASE_URL}")
    private String frontendBaseUrl;

    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // security
        http
            .csrf(csrf -> csrf
                .disable())
			.authorizeHttpRequests(authorize -> authorize
                .anyRequest()
                    .permitAll()
            );

        return http.build();
    }


    /**
     * Allow frontend url with any pattern.
     * 
     * @return
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendBaseUrl)
                        .allowedMethods("GET", "POST", "UPDATE", "DELETE");
            }
        };
    }
}