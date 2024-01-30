package de.word_light.document_builder.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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
// TODO: add webhook to linux server for easier deploy

// TODO: 
    // set datasource credentials, but dont write them inside .env
// TODO: update prod .env eventually
// TODO: edit documentation, docker-compose must be used with .env file in same directory
// TODO: figure docker-compose args out, optimize main pipeline
public class SecurityConfig {
    
    @Value("${GATEWAY_BASE_URL}")
    private String GATEWAY_BASE_URL;

    @Value("${API_MAPPING}")
    private String API_MAPPING;

    @Value("${ENV}")
    private String ENV;

    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // enable csrf in prod only
        if (ENV.equalsIgnoreCase("prod")) {
            // necessary for csrf token to be passed on every request as cooky to browser
            CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
            requestHandler.setCsrfRequestAttributeName(null);

            http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler));

        } else
            http.csrf(csrf -> csrf.disable());
        
        // routes
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