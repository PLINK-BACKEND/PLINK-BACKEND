package com.plink.backend.user.entity;

import com.plink.backend.user.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role; // USER or GUEST

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // 게스트 만료 시간 (회원이면 null)
    // Soft delete 등 확장 대비용 필드
    private LocalDateTime deletedAt;

    // 게임 성공 여부 필드
    @Column(nullable = false)
    private boolean gameSuccess = false;

    // 회원이 참여한 행사 리스트
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<UserFestival> festivals = new ArrayList<>();

    // 게스트 → 회원 전환 메서드
    public void updateToUser(String email, String encodedPassword, String newProfileImageUrl) {
        this.email = email;
        this.password = encodedPassword;
        this.role = Role.USER;
        this.expiresAt = null; // 회원은 만료시간 제거

        // 프로필 이미지가 null이 아닐 경우에만 새로 적용
        if (newProfileImageUrl != null && !newProfileImageUrl.isEmpty()) {
            this.profileImageUrl = newProfileImageUrl;
        }
    }

    public boolean isGuestExpired() {
        return role == Role.GUEST && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public void extendGuestSession() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void addFestival(UserFestival festival) {
        if (festivals == null) {
            festivals = new ArrayList<>();
        }
        if (!festivals.contains(festival)) { // 중복 방지 안전장치
            festivals.add(festival);
            festival.setUser(this);
    }
}}
