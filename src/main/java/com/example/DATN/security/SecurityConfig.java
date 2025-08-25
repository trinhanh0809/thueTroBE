package com.example.DATN.security;

import com.example.DATN.service.JWT.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;   // 👈 import này
import org.springframework.security.crypto.password.PasswordEncoder;      // 👈 và import này
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // 👇 Bean BCrypt (đây là cái bạn đang thiếu)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // strength mặc định 10
    }

    // 👇 (Khuyến nghị) gắn UserDetailsService + PasswordEncoder vào DaoAuthenticationProvider
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationProvider authProvider) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(c -> c.configurationSource(corsConfig()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        // Cho preflight CORS
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger
                        .requestMatchers(Endpoints.SWAGGER).permitAll()

                        // PUBLIC theo method
                        .requestMatchers(HttpMethod.GET, Endpoints.PUBLIC_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, Endpoints.PUBLIC_POST).permitAll()
                        .requestMatchers(HttpMethod.PUT, Endpoints.PUBLIC_PUT).permitAll()
                        .requestMatchers(HttpMethod.DELETE, Endpoints.PUBLIC_DELETE).permitAll()

                        // Role-based
                        .requestMatchers(Endpoints.ADMIN_ENDPOINT).hasRole("ADMIN")
                        .requestMatchers(Endpoints.HOST_ENDPOINT).hasAnyRole("ADMIN","HOST")

                        // Còn lại
                        .anyRequest().authenticated()
                )

                // dùng authentication provider (có BCrypt)
                .authenticationProvider(authProvider)
                // filter JWT
                .addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfig() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(Endpoints.FRONTEND_HOST, "*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        cfg.setAllowCredentials(true);
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

}
