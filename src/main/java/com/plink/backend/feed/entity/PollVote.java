package com.plink.backend.feed.entity;

import com.plink.backend.user.entity.User;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"poll_id", "voter_id"})})
public class PollVote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id", nullable = false)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private PollOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", nullable = false)
    private User voter;

    private LocalDateTime createdAt = LocalDateTime.now();

}
