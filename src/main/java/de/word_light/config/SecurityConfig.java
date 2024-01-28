package de.word_light.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableWebSecurity
// TODO: 
    // check csrf
    // enable ssl?
    // set datasource credentials, but dont write them inside .env
// TODO: update prod .env eventually
// TODO: add artifact id to folder structure
// TODO: register for eureka somehow
public class SecurityConfig {
    
    @Value("${GATEWAY_BASE_URL}")
    private String GATEWAY_BASE_URL;

    @Value("${CSRF_ENABLED}")
    private String CSRF_ENABLED;

    @Value("${API_MAPPING}")
    private String API_MAPPING;

    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // enable csrf in prod only
        // TODO: replace this with env variable, remove CSRF env var
        if (CSRF_ENABLED.equalsIgnoreCase("true"))
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
     * Allow methods {@code GET, POST, UPDATE, DELETE}, origins {@code GATEWAY_BASE_URL}, headers {@code "*"}, credentials and
     * only mappings for {@link #API_MAPPING}.
     * 
     * @return the configured {@link CorsConfigurationSource}
     */
    private CorsConfigurationSource corsConfig() {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(GATEWAY_BASE_URL));
        configuration.setAllowedMethods(List.of("GET", "POST", "UPDATE", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/" + API_MAPPING + "/**", configuration);

        return source;
    }
}