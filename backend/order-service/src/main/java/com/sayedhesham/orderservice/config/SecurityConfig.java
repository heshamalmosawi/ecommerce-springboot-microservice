package com.sayedhesham.orderservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sayedhesham.orderservice.service.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain for the application.
     *
     * This method sets up the HTTP security configuration using Spring
     * Security. It disables CSRF protection and defines authorization rules for
     * incoming HTTP requests:
     *
     * - Requires authentication for all requests to all endpoints.
     *
     * @param http the {@link HttpSecurity} object used to configure security
     * settings.
     * @return the configured {@link SecurityFilterChain} instance.
     * @throws Exception if an error occurs during the configuration process.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("Configuring security filter chain...");
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeHttpRequestsCustomizer -> authorizeHttpRequestsCustomizer
                .requestMatchers(HttpMethod.GET, "/greeting").permitAll()
                .requestMatchers(HttpMethod.GET, "/analytics/seller-summary").hasRole("SELLER")
                .requestMatchers(HttpMethod.GET, "/seller").hasRole("SELLER")
                .requestMatchers(HttpMethod.PATCH, "/{orderId}/status").hasRole("SELLER")
                .anyRequest().authenticated()
                )
                .sessionManagement(sesh -> sesh.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
