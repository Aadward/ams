package com.ams.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.info("Initializing BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain with RBAC");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints - public
                        .requestMatchers("/api/auth/**").permitAll()

                        // Role management - ADMIN only
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")

                        // Backup - ADMIN, MANAGER
                        .requestMatchers("/api/backup/**").hasAnyRole("ADMIN", "MANAGER")

                        // Departments - ADMIN, MANAGER write; authenticated read
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")

                        // Employees - ADMIN, MANAGER write; authenticated read
                        .requestMatchers(HttpMethod.GET, "/api/employees/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/employees/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")

                        // Assets - ADMIN, MANAGER write; authenticated read
                        .requestMatchers(HttpMethod.GET, "/api/assets/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/assets/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/assets/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/assets/**").hasRole("ADMIN")

                        // Dashboard - ADMIN, MANAGER
                        .requestMatchers("/api/dashboard/**").hasAnyRole("ADMIN", "MANAGER")

                        // Maintenance - ADMIN, MANAGER
                        .requestMatchers("/api/maintenance/**").hasAnyRole("ADMIN", "MANAGER")

                        // Excel import/export - ADMIN, MANAGER
                        .requestMatchers("/api/excel/**").hasAnyRole("ADMIN", "MANAGER")

                        // Approvals - ADMIN, MANAGER for pending/approve/reject; authenticated for create/my
                        .requestMatchers("/api/approvals/pending").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/approvals/*/approve").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/approvals/*/reject").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/approvals/**").authenticated()

                        // Notifications - authenticated users
                        .requestMatchers("/api/notifications/**").authenticated()

                        // Actuator health - public
                        .requestMatchers("/actuator/**").permitAll()

                        // Swagger docs - public
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured with RBAC");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        log.debug("CORS configuration set to allow all origins");
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        log.info("Initializing AuthenticationManager");
        return authConfig.getAuthenticationManager();
    }
}
