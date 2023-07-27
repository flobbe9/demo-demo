package com.example.vorspiel_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configuration class to authentiacate requests.
 * 
 * @since 0.0.1
 */
@Configuration
@EnableMethodSecurity // for @PreAuthorize
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http
            .csrf(csrf -> csrf
                .disable())
			.authorizeHttpRequests(authorize -> authorize
                .anyRequest()
                    .permitAll()
            );

        return http.build();
    }
}