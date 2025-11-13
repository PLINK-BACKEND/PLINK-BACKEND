package com.plink.backend.auth.service;

import com.plink.backend.commonS3.S3UploadResult;
import com.plink.backend.auth.dto.LoginRequest;
import com.plink.backend.auth.dto.SignUpRequest;
import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.commonS3.S3Service;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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

        // 1. slug가 있는 경우: 게스트 → 회원 전환
        if (slug != null && !slug.isBlank()) {
            // 같은 slug + nickname으로 존재하는 게스트 찾기
            User guest = userFestivalRepository.findByNicknameAndFestivalSlug(request.getNickname(), slug)
                    .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "게스트 계정을 찾을 수 없습니다."));

            if (guest.getRole() != Role.GUEST) {
                throw new CustomException(HttpStatus.CONFLICT, "이미 회원으로 등록된 닉네임입니다.");
            }

            // 프로필 업로드
            String imageUrl = guest.getProfileImageUrl();
            MultipartFile profileImage = request.getProfileImage();

            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    S3UploadResult uploadResult = s3Service.upload(profileImage, "profiles");
                    imageUrl = uploadResult.getUrl() != null
                            ? uploadResult.getUrl()
                            : s3BaseUrl + "/" + uploadResult.getKey();
                } catch (Exception e) {
                    log.error("S3 업로드 실패: {}", e.getMessage(), e);
                    throw new IllegalStateException("S3 업로드 실패로 회원가입이 중단되었습니다.");
                }
            }

            // 기존 게스트 정보를 회원 정보로 전환
            guest.setEmail(request.getEmail());
            guest.setPassword(passwordEncoder.encode(request.getPassword()));
            guest.setProfileImageUrl(imageUrl);
            guest.setRole(Role.USER);
            guest.setExpiresAt(null);

            userRepository.save(guest);


            // UserFestival은 이미 존재하므로 그대로 반환
            UserFestival existingFestival = userFestivalRepository.findByUser_UserId(guest.getUserId())
                    .stream()
                    .filter(f -> f.getFestivalSlug().equals(slug))
                    .findFirst()
                    .orElse(null);

            // 세션을 회원으로 갱신
            session.setAttribute("user", guest);
            session.removeAttribute("guest");

            return new UserResponse(guest, existingFestival);

        }

        // 2. slug가 없는 경우: 일반 회원가입
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // 프로필 업로드 (프론트가 항상 보냄 → 실패 시 예외 발생해야 함)
        String imageUrl = null;
        try {
            S3UploadResult uploadResult = s3Service.upload(request.getProfileImage(), "profiles");
            imageUrl = uploadResult.getUrl() != null
                    ? uploadResult.getUrl()
                    : s3BaseUrl + "/" + uploadResult.getKey();
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("S3 업로드 실패로 회원가입이 중단되었습니다.");
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

        userRepository.save(user);

        // 세션에 회원 등록 (회원으로 전환)
        session.setAttribute("user", user);
        // 축제 정보는 join 시점에 등록하므로 null 반환
        return new UserResponse(user, null);
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

        List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(user.getUserId());
        UserFestival festival = festivals.isEmpty() ? null : festivals.get(0);

        return new UserResponse(user, festival);
    }

    // 로그아웃(세션 무효화)
    @Transactional
    public void logout() {
        session.invalidate();
    }

    // 게스트 계정 생성
    @Transactional
    public UserResponse createGuest(String slug, String nickname, MultipartFile profileImage) throws IOException {

        String imageUrl;
        try {
            S3UploadResult uploadResult = s3Service.upload(profileImage, "guest-profiles");
            imageUrl = uploadResult.getUrl() != null
                    ? uploadResult.getUrl()
                    : s3BaseUrl + "/" + uploadResult.getKey();
        } catch (Exception e) {
            log.error("S3 업로드 실패: {}", e.getMessage(), e);
            throw new IllegalStateException("게스트 프로필 이미지 업로드 중 오류가 발생했습니다.");
        }

        User guest = User.builder()
                .email(null)
                .password("") // 비밀번호 없음
                .profileImageUrl(imageUrl)
                .role(Role.GUEST)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        userRepository.save(guest);

        // 세션 등록 (게스트 상태로 식별)
        session.setAttribute("guest", guest);
        // UserFestival은 아직 없으므로 null
        return new UserResponse(guest, null);
    }

}
