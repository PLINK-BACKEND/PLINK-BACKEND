package com.plink.backend.auth.service;

import com.plink.backend.auth.dto.GuestRequest;
import com.plink.backend.auth.dto.LoginRequest;
import com.plink.backend.auth.dto.SignUpRequest;
import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.repository.UserRepository;
import com.plink.backend.user.role.Role;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plink.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;

    // 회원가입
    @Transactional(rollbackFor = CustomException.class)
    public UserResponse signUp(SignUpRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(req.getNickname())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .nickname(req.getNickname())
                .password(passwordEncoder.encode(req.getPassword()))
                .profileImageUrl(req.getProfileImageUrl())
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return new UserResponse(user);
    }

    // 회원 로그인
    @Transactional
    public UserResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        session.setAttribute("user", user);
        return new UserResponse(user);
    }

    // 로그아웃(세션 무효화)
    @Transactional
    public void logout() {
        session.invalidate();
    }

    // 게스트 계정 생성
    @Transactional
    public UserResponse createGuest(GuestRequest req) {
        // 1️. 랜덤한 게스트 ID 생성
        String guestId = "guest-" + UUID.randomUUID().toString().substring(0, 8);

        // 2️. 닉네임과 이미지가 전달되지 않았다면 기본값 설정
        String nickname = (req.getNickname() != null && !req.getNickname().isBlank())
                ? req.getNickname()
                : "게스트_" + guestId.substring(6);

        String profileImageUrl = (req.getProfileImageUrl() != null && !req.getProfileImageUrl().isBlank())
                ? req.getProfileImageUrl()
                : "/images/default_guest.png";

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        // 3️. User 엔티티 생성
        User guest = User.builder()
                .email(guestId)
                .nickname(nickname)
                .password("") // 게스트이므로 비밀번호 없음
                .profileImageUrl(profileImageUrl)
                .role(Role.GUEST)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        // 4️. 저장 및 세션 등록
        userRepository.save(guest);
        session.setAttribute("guest", guest);

        return new UserResponse(guest);
    }


    // 게스트 -> 회원 데이터 마이그레이션
    @Transactional
    public void migrateGuestToUser(String guestId, User user) {
        userRepository.findByEmail(guestId).ifPresent(guest -> {
            // 여기에 게스트 관련 데이터(예: 점수, 피드 등) 이전 로직 추가 가능
            userRepository.delete(guest);
        });
    }
}
