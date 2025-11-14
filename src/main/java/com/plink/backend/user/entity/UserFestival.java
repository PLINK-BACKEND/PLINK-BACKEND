package com.plink.backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_festival",
        uniqueConstraints = { // 닉네임 중복불가 조합은 slug, nickname으로 구별.
                @UniqueConstraint(columnNames = {"festival_slug", "nickname"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFestival { // 한 유저는 여러 행사에 참여할 수 있음

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User와 다대일 관계로 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "festival_slug", nullable = false)
    private String festivalSlug;

    @Column(nullable = false)
    private String nickname;

    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private boolean secretFrameUnlocked = false;
}
