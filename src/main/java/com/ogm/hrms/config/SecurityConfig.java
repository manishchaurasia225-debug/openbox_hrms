package com.ogm.hrms.config;

import com.ogm.hrms.security.JwtAuthenticationFilter;
import com.ogm.hrms.security.JwtService;
import com.ogm.hrms.security.RestAccessDeniedHandler;
import com.ogm.hrms.security.RestAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Central Spring Security configuration with two filter chains:
 *
 * <ol>
 *   <li><b>API chain</b> ({@code /api/**}, order 1) — stateless, JWT-authenticated, CSRF disabled,
 *       authorization enforced per the RBAC model; unauthenticated/forbidden requests return the
 *       standard {@link com.ogm.hrms.common.ApiResponse} envelope as 401/403.</li>
 *   <li><b>Web chain</b> (everything else, order 2) — serves the public landing page and any
 *       server-rendered resources with stateful form login.</li>
 * </ol>
 *
 * <p>Method security ({@code @PreAuthorize}) is enabled so services/controllers can enforce
 * fine-grained, permission-based authorization. Passwords use a single shared BCrypt encoder.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(HrmsSecurityProperties.class)
public class SecurityConfig {

    /** API endpoints reachable without authentication (obtaining/renewing tokens). */
    private static final String[] API_PUBLIC_ENDPOINTS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/auth/password-reset/request",
            "/api/v1/auth/password-reset/confirm",
            "/api/v1/auth/email/verify/request",
            "/api/v1/auth/email/verify/confirm"
    };

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
                                                      JwtService jwtService,
                                                      RestAuthenticationEntryPoint authenticationEntryPoint,
                                                      RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(API_PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Public, non-API endpoints: the landing/login pages, static assets, the actuator health/info
     * probes, and the OpenAPI document + Swagger UI (API docs are intentionally reachable so the UI
     * renders; individual API calls still require a JWT via the API chain).
     */
    private static final String[] WEB_PUBLIC_ENDPOINTS = {
            "/", "/login", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error",
            "/actuator/health", "/actuator/health/**", "/actuator/info",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml"
    };

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WEB_PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form.permitAll())
                .logout(logout -> logout.permitAll());
        return http.build();
    }

    /**
     * Shared password encoder for hashing/verifying credentials — one consistent strategy everywhere.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS policy for the API chain, driven by {@link HrmsSecurityProperties.Cors}. When no origins
     * are configured the source registers no mappings, so {@code .cors()} is a no-op and no
     * cross-origin access is granted — the safe default. Configure origins per environment
     * (dev SPA server, staging/prod web hosts); never use a wildcard alongside credentials.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(HrmsSecurityProperties properties) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        HrmsSecurityProperties.Cors cors = properties.cors();
        if (cors.allowedOrigins().isEmpty()) {
            return source; // no mappings -> no CORS headers emitted (behaviour unchanged)
        }
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(cors.allowedOrigins());
        configuration.setAllowedMethods(cors.allowedMethods());
        configuration.setAllowedHeaders(cors.allowedHeaders());
        configuration.setExposedHeaders(cors.exposedHeaders());
        configuration.setAllowCredentials(cors.allowCredentials());
        configuration.setMaxAge(cors.maxAge().getSeconds());
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
