package com.plink.backend.game.controller;

import com.plink.backend.game.dto.GameRankingResponse;
import com.plink.backend.game.dto.GameScoreRequest;
import com.plink.backend.game.dto.GameScoreResponse;
import com.plink.backend.game.service.GameService;
import com.plink.backend.game.websocket.GameWebSocketHandler;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{slug}/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameWebSocketHandler gameWebSocketHandler;

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


    // 게임 완료 시점 테스트용
    @PostMapping("/clear")
    public void clearGame(
            @PathVariable String slug,
            @RequestParam String nickname
    ) {
        String message = nickname + "님이 게임을 클리어했습니다!";
        gameWebSocketHandler.broadcastToSlug(slug, message);
        System.out.println("[broadcast] " + message);
    }
}
