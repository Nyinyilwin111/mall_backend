package com.sein_gar_har.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/notifications/**").permitAll()
                        .requestMatchers("/topic/notifications/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/chats/**").permitAll()
                        .requestMatchers("/api/sms/**").permitAll()
                        .requestMatchers("/api/push/user/**").permitAll()
                        .requestMatchers("/api/push/vapidPublicKey").permitAll() // public
                        .requestMatchers("/api/push/subscribe").permitAll()      // public
                        .requestMatchers("/api/push/sendAll").authenticated()    // JWT required
                        .requestMatchers("/api/push/**").authenticated() // other push endpoints require auth
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/branches/**").permitAll()
                        .requestMatchers("/api/spaces/**").permitAll()
                        .requestMatchers("/api/reports/**").permitAll()
                        .requestMatchers("/api/space-types/**").permitAll()
                        .requestMatchers("/api/floors/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auditlogs/**").permitAll()
                        .requestMatchers("/api/utilities/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthorizationFilter, BasicAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration cfg = new CorsConfiguration();
                    cfg.setAllowedOrigins(List.of("http://localhost:5173"));
                    cfg.setAllowedMethods(Collections.singletonList("*"));
                    cfg.setAllowCredentials(true);
                    cfg.setAllowedHeaders(Collections.singletonList("*"));
                    cfg.setExposedHeaders(List.of(JwtConstants.TOKEN_HEADER));
                    cfg.setMaxAge(3600L);
                    return cfg;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }


    // Expose AuthenticationManager bean (required for AuthController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
