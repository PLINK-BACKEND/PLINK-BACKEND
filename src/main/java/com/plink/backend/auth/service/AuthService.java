package com.plink.backend.auth.service;

import com.plink.backend.auth.dto.GuestRequest;
import com.plink.backend.auth.dto.LoginRequest;
import com.plink.backend.auth.dto.SignUpRequest;
import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.commonService.S3Service;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.repository.UserRepository;
import com.plink.backend.user.role.Role;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plink.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    // 회원가입
    @Transactional
    public UserResponse signUp(String email, String nickname, String password, MultipartFile profileImage) throws IOException {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // S3 업로드
        String imageKey = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            imageKey = s3Service.upload(profileImage, "profiles");
        }

        // S3 url 생성
        String imageUrl = (imageKey != null)
                ? s3BaseUrl + "/" + imageKey
                : "/images/default.png";


        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .profileImageUrl(imageUrl)
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
    public UserResponse createGuest(String nickname, MultipartFile profileImage) throws IOException {
        String guestId = "guest-" + UUID.randomUUID().toString().substring(0, 8);

        // S3 업로드
        String imageKey = (profileImage != null && !profileImage.isEmpty())
                ? s3Service.upload(profileImage, "guest-profiles")
                : null;

        String imageUrl = (imageKey != null)
                ? s3BaseUrl + "/" + imageKey
                : "/images/default_guest.png";

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        // User 엔티티 생성
        User guest = User.builder()
                .email(guestId)
                .nickname(nickname)
                .password("")
                .profileImageUrl(imageUrl)
                .role(Role.GUEST)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        // 정보 저장 및 세션 등록
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
