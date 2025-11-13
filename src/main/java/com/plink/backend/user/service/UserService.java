package com.plink.backend.user.service;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserFestivalRepository userFestivalRepository;

    // 이메일로 유저정보 조회, 가장 최근에 참여한 행사정보 반환
    public UserResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

        List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(user.getUserId());
        UserFestival recentFestival = festivals.isEmpty() ? null : festivals.get(festivals.size() - 1);

        return new UserResponse(user, recentFestival);
    }

    // ID로 유저정보 조회, 가장 최근에 참여한 행사정보 반환
    public UserResponse getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: id=" + id));

        List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(user.getUserId());
        UserFestival recentFestival = festivals.isEmpty() ? null : festivals.get(festivals.size() - 1);

        return new UserResponse(user, recentFestival);
    }

    // 특정 slug 기준으로 유저정보 조회, 한 유저가 여러행사 참여했을 수 있으므로 slug 지정 가능하다!
    public UserResponse getUserInfoBySlug(Long userId, String slug) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: id=" + userId));

        UserFestival festival = userFestivalRepository
                .findByUser_UserId(userId)
                .stream()
                .filter(f -> f.getFestivalSlug().equals(slug))
                .findFirst()
                .orElse(null);

        return new UserResponse(user, festival);
    }


    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // 닉네임 중복 판별
    public boolean isNicknameDuplicate(String nickname) {
        return userFestivalRepository.existsByNickname(nickname);
    }
}
