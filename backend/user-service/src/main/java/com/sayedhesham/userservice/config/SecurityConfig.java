package com.sayedhesham.userservice.config;

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

import com.sayedhesham.userservice.service.security.JwtAuthenticationFilter;

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
     * - Allows all GET requests to the "/products" endpoint without
     * authentication. - Requires authentication for all other requests to
     * endpoints under "/products/**". - Permits all other requests to any other
     * endpoints without authentication.
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
                // .requestMatchers("/auth/register", "/auth/login", "/greeting").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/greeting").permitAll()
                .anyRequest().authenticated()
                )
                .sessionManagement(sesh -> sesh.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
