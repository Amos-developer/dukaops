package com.dukaops.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/register",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/team/invitations/*/accept").permitAll()
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/health/**"
                ).permitAll()
                .requestMatchers("/api/settings/subscription", "/api/billing/**").hasRole("OWNER")
                .requestMatchers("/api/refunds/**", "/api/sales/refunds/**").denyAll()
                .requestMatchers("/api/messaging/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/api/tenants", "/api/tenants/").denyAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").authenticated()
                .requestMatchers("/api/categories/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("OWNER")
                .requestMatchers("/api/products/**", "/api/team/**", "/api/contacts/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventory/branches/*/movements", "/api/inventory/branches/*/stock/*/movements").hasAnyRole("OWNER", "ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").authenticated()
                .requestMatchers("/api/inventory/**").hasAnyRole("OWNER", "ADMIN", "MANAGER")
                .requestMatchers("/api/reports/**").hasAnyRole("OWNER", "ADMIN", "MANAGER")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
