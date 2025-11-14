package com.plink.backend.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameScoreRequest {
    private double score;
    private String nickname;
    private boolean success;
}
