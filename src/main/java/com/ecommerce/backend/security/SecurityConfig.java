package com.ecommerce.backend.security;

import com.ecommerce.backend.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        userDetailsService
                );

        http
            .csrf(csrf -> csrf.disable())

            .cors(cors -> {})

            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .formLogin(form -> form.disable())

            .httpBasic(basic -> basic.disable())

            .authorizeHttpRequests(auth -> auth

                    // =========================
                    // ADMIN LOGIN - PUBLIC
                    // =========================
                    .requestMatchers("/api/auth/**")
                    .permitAll()

                    // =========================
                    // CUSTOMER - PLACE ORDER
                    // =========================
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/orders"
                    )
                    .permitAll()

                    // =========================
                    // ADMIN - VIEW ORDERS
                    // =========================
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/orders",
                            "/api/orders/**"
                    )
                    .hasRole("ADMIN")

                    // =========================
                    // ADMIN - UPDATE ORDER STATUS
                    // =========================
                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/orders/*/status"
                    )
                    .hasRole("ADMIN")

                    // =========================
                    // CUSTOMER - VIEW PRODUCTS
                    // =========================
                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/products",
                            "/api/products/**"
                    )
                    .permitAll()

                    // =========================
                    // ADMIN - ADD PRODUCTS
                    // =========================
                    .requestMatchers(
                            HttpMethod.POST,
                            "/api/products",
                            "/api/products/**"
                    )
                    .hasRole("ADMIN")

                    // =========================
                    // ADMIN - UPDATE PRODUCTS
                    // =========================
                    .requestMatchers(
                            HttpMethod.PUT,
                            "/api/products",
                            "/api/products/**"
                    )
                    .hasRole("ADMIN")

                    // =========================
                    // ADMIN - DELETE PRODUCTS
                    // =========================
                    .requestMatchers(
                            HttpMethod.DELETE,
                            "/api/products",
                            "/api/products/**"
                    )
                    .hasRole("ADMIN")

                    // =========================
                    // EVERYTHING ELSE
                    // =========================
                    .anyRequest()
                    .authenticated()
            )

            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}