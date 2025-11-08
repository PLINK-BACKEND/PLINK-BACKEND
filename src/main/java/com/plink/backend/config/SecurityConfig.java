package com.plink.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (Postman 테스트용)
                .csrf(csrf -> csrf.disable())

                // URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",        // 로그인, 회원가입, 게스트
                                "/user/info",     // 로그인한 유저 조회용
                                "/ws/**",         // 웹소켓 엔드포인트
                                "/error",           // 오류 페이지 등등
                                "/*/games/**"
                                "/error",          // 오류 페이지 등등
                                "/fourcuts/**"

                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/{slug}/posts", "/{slug}/posts/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 로그인/기본 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 세션 관리 설정
                .sessionManagement(session -> session
                        .maximumSessions(1)
                );

        return http.build();
    }
}
