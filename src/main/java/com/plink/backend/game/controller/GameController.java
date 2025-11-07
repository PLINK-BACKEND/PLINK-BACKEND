package com.plink.backend.game.controller;

import com.plink.backend.game.dto.GameRankingResponse;
import com.plink.backend.game.dto.GameScoreRequest;
import com.plink.backend.game.dto.GameScoreResponse;
import com.plink.backend.game.service.GameService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/{slug}/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    // 게임 점수 등록
    @PostMapping("/{gameId}/score")
    public ResponseEntity<Void> submitScore(
            @PathVariable String slug,
            @PathVariable Long gameId,
            @RequestBody GameScoreRequest request,
            @AuthenticationPrincipal User user
    ) {
        gameService.submitScore(slug, gameId, request, user);
        return ResponseEntity.ok().build();
    }

    // 게임 랭킹 조회
    @GetMapping("/{gameId}/ranking")
    public ResponseEntity<GameRankingResponse> getRanking(
            @PathVariable String slug,
            @PathVariable Long gameId
    ) {
        return ResponseEntity.ok(gameService.getRanking(slug, gameId));
    }

    // 내 점수 및 등수 조회
    @GetMapping("/{gameId}/me")
    public ResponseEntity<GameScoreResponse> getMyScore(
            @PathVariable String slug,
            @PathVariable Long gameId,
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(gameService.getMyScore(slug, gameId, nickname));
    }
}
