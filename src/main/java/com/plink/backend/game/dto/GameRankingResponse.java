package com.plink.backend.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class GameRankingResponse {
    private String festivalSlug;
    private Long gameId;
    private List<GameScoreResponse> rankings;
}
