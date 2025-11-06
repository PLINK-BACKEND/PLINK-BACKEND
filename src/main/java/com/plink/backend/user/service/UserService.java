package com.plink.backend.user.service;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 이메일로 유저 정보 조회
    public UserResponse getUserInfoByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));
        return new UserResponse(user);
    }

    // ID 로 유저 정보 조회
    public UserResponse getUserInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: id=" + id));
        return new UserResponse(user);
    }
}
