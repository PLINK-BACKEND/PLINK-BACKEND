package com.plink.backend.config;

import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();

            // ✔ 프론트엔드 도메인 (HTTPS)
            config.setAllowedOrigins(List.of(
                "*"
                   // "http://localhost:5173",              // 로컬 개발
                   // "https://plink-2025.netlify.app"      // 배포된 프론트
            ));

            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));

            // ✔ JSESSIONID 쿠키 허용
            config.setAllowCredentials(true);

            // ✔ Set-Cookie 헤더 프론트에서 읽을 수 있게 허용
            config.setExposedHeaders(List.of("Set-Cookie"));

            return config;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CORS (위에서 만든 게 적용됨)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",         // 로그인/회원가입/게스트
                                "/user/info",
                                "/ws/**",
                                "/ws/feed/**",
                                "/topic/**",
                                "/error",
                                "/*/games/**",
                                "/fourcuts/**",
                                "/api/festival/**",
                                "/favicon.ico",
                                "/plink/festivals"
                        ).permitAll()

                        // 공개 GET 요청
                        .requestMatchers(HttpMethod.GET,
                                "/{slug}/posts", "/{slug}/posts/**",
                                "/{slug}/main", "/{slug}/main/**"
                        ).permitAll()

                        // 인증 필요한 요청
                        .requestMatchers(HttpMethod.PATCH, "/*/mypage/profile").authenticated()
                        .requestMatchers(HttpMethod.POST, "/*/posts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/*/posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/*/posts/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/*/reports/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/*/comments/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/*/comments/*").authenticated()

                        .anyRequest().authenticated()
                )

                // 기본 로그인/HTTP Basic 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 정책
                .sessionManagement(session ->
                        session
                                .maximumSessions(1)
                );

        return http.build();
    }
}
