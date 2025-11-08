package com.plink.backend.feed.entity;

import com.plink.backend.user.entity.User;
import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"poll_id", "voter_id"})})
public class PollVote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Poll poll;

    @ManyToOne(fetch = FetchType.LAZY)
    private PollOption option;

    @ManyToOne(fetch = FetchType.LAZY)
    private User voter;

    private LocalDateTime createdAt = LocalDateTime.now();

}
