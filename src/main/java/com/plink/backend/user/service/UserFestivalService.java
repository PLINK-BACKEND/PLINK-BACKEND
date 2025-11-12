package com.plink.backend.user.service;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.dto.JoinFestivalRequest;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 로그인한 사용자가 축제에 참여하는 경우 -> slug, nickname 받고 db에 저장하기
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFestivalService {

    private final UserFestivalRepository userFestivalRepository;

    @Transactional
    public UserResponse joinFestival(User user, JoinFestivalRequest request) {
        String slug = request.getSlug();
        String nickname = request.getNickname();

        if (slug == null || slug.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "slug 값이 누락되었습니다.");
        }

        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "닉네임 값이 누락되었습니다.");
        }

        // 닉네임 중복 검사 (같은 slug 내에서)
        if (userFestivalRepository.existsByFestivalSlugAndNickname(slug, nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이 닉네임은 이미 사용 중입니다.");
        }

        // 유저가 이미 참여 중인지 검사
        if (userFestivalRepository.existsByUserAndFestivalSlug(user, slug)) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 참여한 축제입니다.");
        }

        // 새 UserFestival 생성
        UserFestival festival = UserFestival.builder()
                .user(user)
                .festivalSlug(slug)
                .nickname(nickname)
                .joinedAt(LocalDateTime.now())
                .build();

        user.addFestival(festival);
        userFestivalRepository.save(festival);

        return new UserResponse(user, festival);
    }
}
