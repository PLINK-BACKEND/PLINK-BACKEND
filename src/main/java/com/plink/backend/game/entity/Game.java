package com.plink.backend.game.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String festivalSlug; // 어떤 행사(slug)에 속하는 게임인지

    @Column(nullable = false)
    private String name; // 게임 이름

    private String description;
}
