package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class PollOption {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poll_id")
    private Poll poll;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int voteCount = 0;

    void setPoll(Poll poll) { this.poll = poll; }

}
