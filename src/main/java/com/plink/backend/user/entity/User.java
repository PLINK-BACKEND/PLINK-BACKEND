package com.plink.backend.user.entity;

import com.plink.backend.user.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private String password;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role; // USER or GUEST

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // 게스트 만료 시간 (회원이면 null)
    // Soft delete 등 확장 대비용 필드
    private LocalDateTime deletedAt;

    // 회원이 참여한 행사 리스트
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserFestival> festivals = new ArrayList<>();

    public boolean isGuestExpired() {
        return role == Role.GUEST && expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public void extendGuestSession() {
        this.expiresAt = LocalDateTime.now().plusHours(24);
    }

    public void addFestival(UserFestival festival) {
        if (festivals == null) {
            festivals = new ArrayList<>();
        }
        festivals.add(festival);
        festival.setUser(this);
    }
}
