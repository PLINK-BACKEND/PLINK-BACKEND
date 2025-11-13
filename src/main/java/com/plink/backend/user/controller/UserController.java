package com.plink.backend.user.controller;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final HttpSession session;
    private final UserFestivalRepository userFestivalRepository;

    @GetMapping("/info")
    public ResponseEntity<UserResponse> getUserInfo() {
        Object userObj = session.getAttribute("user");
        Object guestObj = session.getAttribute("guest");

        if (userObj == null && guestObj == null) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        if (userObj instanceof User user) {
            // 회원: 가장 최근 참여 행사 정보 조회
            List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(user.getUserId());
            UserFestival recentFestival = festivals.isEmpty() ? null : festivals.get(festivals.size() - 1);

            return ResponseEntity.ok(new UserResponse(user, recentFestival));
        }

        if (guestObj instanceof User guest) {
            // 게스트: slug 1개만 존재하므로 바로 조회
            List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(guest.getUserId());
            UserFestival guestFestival = festivals.isEmpty() ? null : festivals.get(0);

            return ResponseEntity.ok(new UserResponse(guest, guestFestival));
        }

        throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "세션 정보가 손상되었습니다.");
    }


}
