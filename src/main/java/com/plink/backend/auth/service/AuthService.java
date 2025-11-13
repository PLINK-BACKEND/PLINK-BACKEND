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

        // 세션에 게스트가 있는 경우 → 신규 회원 생성 대신 업그레이드 수행
        User guest = (User) session.getAttribute("guest");
        if (guest != null) {
            return upgradeGuestToUser(guest, request);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }
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
                S3UploadResult uploadResult = s3Service.upload(request.getProfileImage(), "profiles");

                // S3uploadResult 객체에 URL 필드가 포함되어 있으면 그대로 사용
                if (uploadResult.getUrl() != null) {
                    imageUrl = uploadResult.getUrl();
                } else {
                    // URL 필드가 없을 경우 직접 구성
                    imageUrl = s3BaseUrl + "/" + uploadResult.getKey();
                }

            } catch (Exception e) {
                log.error("S3 업로드 실패: {}", e.getMessage(), e);
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

    // GUEST -> USER 승격 로직
    @Transactional
    public UserResponse upgradeGuestToUser(User guest, SignUpRequest request) throws IOException {

        // (1) 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // (2) 프로필 업로드 여부 판단
        String imageUrl = guest.getProfileImageUrl();
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {

            S3UploadResult uploadResult = s3Service.upload(request.getProfileImage(), "profiles");
            if (uploadResult.getUrl() != null) {
                imageUrl = uploadResult.getUrl();
            } else {
                imageUrl = s3BaseUrl + "/" + uploadResult.getKey();
            }
        }

        // (3) 새 User 생성 (정식 회원으로)
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(imageUrl)
                .role(Role.USER)
                .createdAt(guest.getCreatedAt())    // 게스트 생성시간 유지
                .build();
        userRepository.save(newUser);
        // (5) 기존 게스트의 UserFestival 조회
                List<UserFestival> guestFestivals = userFestivalRepository.findByUser_UserId(guest.getUserId());

                String newNickname = request.getNickname();

                for (UserFestival festival : guestFestivals) {

                    // user 소유권을 newUser로 변경
                    festival.setUser(newUser);

                    // nickname 업데이트
                    if (newNickname != null && !newNickname.isBlank()) {
                        if (userFestivalRepository.existsByFestivalSlugAndNickname(
                                festival.getFestivalSlug(), newNickname)) {

                            if (!festival.getNickname().equals(newNickname)) {
                                throw new CustomException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
                            }
                        }
                        festival.setNickname(newNickname);
                    }

                    // 변경을 즉시 DB에 반영
                    userFestivalRepository.save(festival);
                }

        // guest 삭제는 반드시 소유권 변경/저장 후에 실행해야 함!
                // 삭제 대신 guest 비활성화 처리
                guest.setDeletedAt(LocalDateTime.now());
                guest.setExpiresAt(null);
                guest.setRole(Role.DELETED); // 새 ENUM 추가 추천

                session.removeAttribute("guest");


        // (7) 세션에서 guest 제거
        session.removeAttribute("guest");

        // (8) 대표 축제를 가져오기
        UserFestival festival =
                userFestivalRepository.findByUser_UserId(newUser.getUserId())
                        .stream()
                        .findFirst()
                        .orElse(null);

        return new UserResponse(newUser, festival);
    }

    // 회원 로그인
    @Transactional
    public UserResponse login(LoginRequest req, HttpServletRequest request) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        // UserFestival을 userId 기반으로 fresh하게 DB에서 다시 조회
        List<UserFestival> festivals = userFestivalRepository.findByUser_UserId(user.getUserId());
        UserFestival festival = festivals.isEmpty() ? null : festivals.get(0);

        // Spring Security 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());

        // SecurityContext에 인증 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 세션에 SecurityContext 저장
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        // UserResponse는 newUser + festival이 확정적으로 들어갈 수 있게 됨
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
        String guestId = "guest-" + UUID.randomUUID().toString().substring(0, 8);

        // 행사 내 닉네임 중복 체크
        if (userFestivalRepository.existsByFestivalSlugAndNickname(slug, nickname)) {
            throw new CustomException(HttpStatus.CONFLICT, "이 행사의 닉네임은 이미 사용 중입니다.");
        }

        String imageUrl;

        if (profileImage != null && !profileImage.isEmpty()) {
            try {

                S3UploadResult uploadResult = s3Service.upload(profileImage, "guest-profiles");

                // uploadResult 안에 url 필드가 있으면 그대로 사용, 없으면 s3BaseUrl 조합
                if (uploadResult.getUrl() != null) {
                    imageUrl = uploadResult.getUrl();
                } else {
                    imageUrl = s3BaseUrl + "/" + uploadResult.getKey();
                }

            } catch (Exception e) {
                log.error("S3 업로드 실패: {}", e.getMessage(), e);
                throw new IllegalStateException("게스트 프로필 이미지 업로드 중 오류가 발생했습니다.");
            }
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

}
