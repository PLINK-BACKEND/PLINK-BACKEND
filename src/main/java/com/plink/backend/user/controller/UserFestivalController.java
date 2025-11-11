package com.plink.backend.user.controller;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.user.dto.JoinFestivalRequest;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.service.UserFestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 회원가입 이후 로그인한 사용자가 축제에 접근할 때는, 기존에 받지 못한 slug, nickname 값을 받아야 함.
@RestController
@RequestMapping("/user/festival")
@RequiredArgsConstructor
public class UserFestivalController {

    private final UserFestivalService userFestivalService;

    // 축제 참여 등록
    @PostMapping("/join")
    public ResponseEntity<UserResponse> joinFestival(
            @AuthenticationPrincipal User user,
            @RequestBody JoinFestivalRequest request
    ) {
        UserResponse response = userFestivalService.joinFestival(user, request);
        return ResponseEntity.ok(response);
    }
}
