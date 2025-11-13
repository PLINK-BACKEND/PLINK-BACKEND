package com.plink.backend.user.service;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.dto.JoinFestivalRequest;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 로그인한 사용자가 축제에 참여하는 경우 -> slug, nickname 받고 db에 저장하기
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserFestivalService {

    private final UserRepository userRepository;
    private final UserFestivalRepository userFestivalRepository;

    // 축제 참여 등록 (slug + nickname)
    public UserResponse joinFestival(User user, JoinFestivalRequest request) {
        String slug = request.getSlug();
        String nickname = request.getNickname();

        // 세션에서 받은 user는 detached 상태이므로, 다시 영속화 시켜줌
        User persistentUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        // 유효성 검사
        if (slug == null || slug.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 축제 slug입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "닉네임은 필수 입력 값입니다.");
        }

        // 닉네임 중복검사 (slug별로, 게스트/회원 통합)
        if (userFestivalRepository.existsByFestivalSlugAndNickname(slug, nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이 축제에서 이미 사용 중인 닉네임입니다.");
        }

        // 새 UserFestival 생성 및 저장
        UserFestival newFestival = UserFestival.builder()
                .user(user)
                .festivalSlug(slug)
                .nickname(nickname)
                .joinedAt(LocalDateTime.now())
                .build();

        user.addFestival(newFestival);
        userRepository.save(user); // Cascade로 UserFestival까지 저장

        log.info("신규 축제 참여 등록 완료: userId={}, slug={}, nickname={}",
                user.getUserId(), slug, nickname);

        // user + userfestival 정보 반환
        return new UserResponse(user, newFestival);
    }
}
