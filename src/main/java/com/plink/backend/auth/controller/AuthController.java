package com.plink.backend.auth.controller;

import com.plink.backend.auth.dto.GuestRequest;
import com.plink.backend.auth.dto.LoginRequest;
import com.plink.backend.auth.dto.SignUpRequest;
import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.auth.service.AuthService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@RequestBody SignUpRequest request) {
        UserResponse userResponse = authService.signUp(request);
        return ResponseEntity.ok(userResponse);
    }

    // 회원 로그인
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // 회원 로그아웃
    @PostMapping("/logout")
    public void logout() {
        authService.logout();
    }

    // 게스트 세션 생성
    @PostMapping("/guest")
    public UserResponse guest(@RequestBody GuestRequest guestRequest) {
        return authService.createGuest(guestRequest);
    }
}
