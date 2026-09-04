package com.syfe.personalfinance.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint
) {

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Disabled for REST API ease of use and testing
            .cors { } // Handled by WebConfig
            .headers { headers -> headers.frameOptions { it.sameOrigin() } } // Required to render H2 Console frames
            .exceptionHandling { it.authenticationEntryPoint(authenticationEntryPoint) }
            .sessionManagement { session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(5) // Protect against session flooding
            }
            .authorizeHttpRequests { auth ->
                auth
                    // Public auth endpoints
                    .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                    // Monitoring & OpenAPI Docs
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    // H2 Console
                    .requestMatchers("/h2-console/**").permitAll()
                    // All other endpoints require authentication
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}
