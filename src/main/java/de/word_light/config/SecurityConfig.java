package de.word_light.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


/**
 * Configuration class to authentiacate requests.<p>
 * 
 * @since 0.0.1
 */
@Configuration
@EnableMethodSecurity // for @PreAuthorize
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${FRONTEND_BASE_URL}")
    private String frontendBaseUrl;

    @Value("${CSRF_ENABLED}")
    private String csrfEnabled;

    
    @Bean
    // TODO: set to true in prod, as soon as login is added
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // enable csrf in prod only
        // TODO: replace this with env variable, remove CSRF env var
        if (csrfEnabled.equalsIgnoreCase("true"))
            http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        else
            http.csrf(csrf -> csrf.disable());
        
        http.authorizeHttpRequests(request -> request
                .anyRequest()
                .permitAll())
            .cors(cors -> cors
                .configurationSource(corsConfig()));

        return http.build();
    }


    /**
     * Allow methods {@code GET, POST, UPDATE, DELETE}, origins {@code frontendBaseUrl}, headers {@code "*"}, credentials and
     * only mappings for {@code /api/documentBuilder/**}.
     * 
     * @return the configured {@link CorsConfigurationSource}
     */
    private CorsConfigurationSource corsConfig() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendBaseUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "UPDATE", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/documentBuilder/**", configuration);

        return source;
    }
}