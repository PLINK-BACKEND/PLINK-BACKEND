package com.plink.backend.user.controller;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.role.Role;
import com.plink.backend.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final HttpSession session;

    @GetMapping("/info")
    public ResponseEntity<UserResponse> getUserInfo() {
        Object userObj = session.getAttribute("user");
        Object guestObj = session.getAttribute("guest");

        if (userObj == null && guestObj == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (userObj instanceof User user) {
            return ResponseEntity.ok(new UserResponse(user));
        }

        if (guestObj instanceof User guest) {
            return ResponseEntity.ok(new UserResponse(guest));
        }

        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "세션 정보가 손상되었습니다.");
    }


}
