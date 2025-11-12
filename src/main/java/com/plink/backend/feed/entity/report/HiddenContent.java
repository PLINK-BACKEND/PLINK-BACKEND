package com.plink.backend.feed.entity.report;

import com.plink.backend.user.entity.UserFestival;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HiddenContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_festival_id")
    private UserFestival user;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    private Long targetId;
}
