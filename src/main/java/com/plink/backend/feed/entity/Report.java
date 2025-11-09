package com.plink.backend.feed.entity;

import com.plink.backend.user.entity.UserFestival;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private UserFestival reporter;

    // 신고 대상 게시글 or 댓글
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportTargetType targetType; // POST, COMMENT

    @Column(nullable = false)
    private Long targetId; // 게시글ID 또는 댓글ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(length = 1000)
    private String details; // 추가 내용

    @CreationTimestamp
    private LocalDateTime createdAt;
}
