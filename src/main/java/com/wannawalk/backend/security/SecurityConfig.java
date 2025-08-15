package com.wannawalk.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection, which is common for stateless APIs
            .csrf(csrf -> csrf.disable())
            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow all requests to endpoints under /api/ to be accessed without authentication
                .requestMatchers("/api/**").permitAll()
                // Require authentication for any other request
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults()); // You can also configure other auth methods like formLogin, etc.
        return http.build();
    }
}
