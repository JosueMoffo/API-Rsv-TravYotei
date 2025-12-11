package com.example.Rsv_TravYotei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                // Désactiver CSRF pour les APIs REST (Swagger, endpoints)
                .ignoringRequestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Permettre tout accès sans authentification
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
}