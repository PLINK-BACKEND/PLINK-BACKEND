package com.plink.backend.user.entity;

import com.plink.backend.user.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true)
    private String email; // 회원 이메일 or 게스트 식별자 (guest-xxxx)

    @Column(unique = true)
    private String nickname;

    private String password;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role; // USER or GUEST

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // 게스트 만료 시간 (회원이면 null)

    // Soft delete 등 확장 대비용 필드
    private LocalDateTime deletedAt;

    public boolean isGuestExpired() {
        return role == Role.GUEST && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public void extendGuestSession() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }
}
