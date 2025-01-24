package com.myapp.userimageapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");

        // Configure the security filter chain
        return http
            .csrf().disable()  // Disables CSRF (important for APIs)
            .authorizeHttpRequests()
                .requestMatchers("/api/register","/api/**")
                .permitAll()  // Allow these endpoints without authentication
                .anyRequest().authenticated()  // All other requests require authentication
            .and()
            .build();
    }
}