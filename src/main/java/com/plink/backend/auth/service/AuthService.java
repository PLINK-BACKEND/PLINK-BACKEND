package com.plink.backend.auth.service;

import com.plink.backend.auth.dto.GuestRequest;
import com.plink.backend.auth.dto.LoginRequest;
import com.plink.backend.auth.dto.SignUpRequest;
import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.commonService.S3Service;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.repository.UserRepository;
import com.plink.backend.user.role.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.plink.backend.global.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserFestivalRepository userFestivalRepository;
    private final PasswordEncoder passwordEncoder;
    private final HttpSession session;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.base-url}")
    private String s3BaseUrl;

    // 회원가입 (User 생성 + UserFestival에 등록)
    @Transactional
    public UserResponse signUp(SignUpRequest request) throws IOException {
        String slug = request.getSlug();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // 행사 내 닉네임 중복 방지
        if (userFestivalRepository.existsByFestivalSlugAndNickname(request.getSlug(), request.getNickname())) {
            throw new CustomException(HttpStatus.CONFLICT, "이 행사의 닉네임은 이미 사용 중입니다.");
        }

        // 프로필 업로드
        String imageUrl = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            try {
                String key = s3Service.upload(request.getProfileImage(), "profiles");
                imageUrl = s3BaseUrl + "/" + key;
            } catch (Exception e) {
                log.error("S3 업로드 실패: {}", e.getMessage());
                throw new IllegalStateException("S3 업로드 실패로 회원가입이 중단되었습니다.");
            }
        }

        // 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(request.getPassword());

        // User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPw)
                .profileImageUrl(imageUrl)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        // UserFestival 생성
        UserFestival festival = UserFestival.builder()
                .user(user)
                .festivalSlug(slug)
                .nickname(request.getNickname())
                .joinedAt(LocalDateTime.now())
                .build();

        user.addFestival(festival); // 양방향 연결
        userRepository.save(user); // Cascade -> festival도 함께 저장됨
        return new UserResponse(user, festival);
    }

    // 회원 로그인
    @Transactional
    public UserResponse login(LoginRequest req, HttpServletRequest request) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // Spring Security 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());

        // SecurityContext에 인증 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 SecurityContext 저장
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        return new UserResponse(user);
    }

    // 로그아웃(세션 무효화)
    @Transactional
    public void logout() {
        session.invalidate();
    }

    // 게스트 계정 생성
    @Transactional
    public UserResponse createGuest(String slug, String nickname, MultipartFile profileImage) throws IOException {
        String guestId = "guest-" + UUID.randomUUID().toString().substring(0, 8);

        // 행사 내 닉네임 중복 체크
        if (userFestivalRepository.existsByFestivalSlugAndNickname(slug, nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이 행사의 닉네임은 이미 사용 중입니다.");
        }

        String imageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            String key = s3Service.upload(profileImage, "guest-profiles");
            imageUrl = s3BaseUrl + "/" + key;
        } else {
            imageUrl = "/images/default_guest.png";
        }

        User guest = User.builder()
                .email(guestId)
                .password("") // 게스트는 비밀번호 없음
                .profileImageUrl(imageUrl)
                .role(Role.GUEST)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        // 게스트용 UserFestival 생성
        UserFestival festival = UserFestival.builder()
                .user(guest)
                .festivalSlug(slug)
                .nickname(nickname)
                .joinedAt(LocalDateTime.now())
                .build();

        guest.addFestival(festival);

        userRepository.save(guest);
        session.setAttribute("guest", guest);

        return new UserResponse(guest, festival);
    }

    // 게스트 -> 회원 데이터 이전 (임시)
    @Transactional
    public void migrateGuestToUser(String guestId, User user) {
        userRepository.findByEmail(guestId).ifPresent(guest -> {
            userRepository.delete(guest);
        });
    }
}
