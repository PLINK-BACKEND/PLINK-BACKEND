package com.plink.backend.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GameScoreResponse {
    private String nickname;
    private double score;
    private int rank;
}
