package com.plink.backend.game.service;

import com.plink.backend.game.dto.GameRankingResponse;
import com.plink.backend.game.dto.GameScoreRequest;
import com.plink.backend.game.dto.GameScoreResponse;
import com.plink.backend.game.entity.Game;
import com.plink.backend.game.entity.GameScore;
import com.plink.backend.game.repository.GameRepository;
import com.plink.backend.game.repository.GameScoreRepository;
import com.plink.backend.game.websocket.GameWebSocketHandler;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final GameWebSocketHandler gameWebSocketHandler; // 웹소켓 방송

    @Transactional
    public void submitScore(String slug, Long gameId, GameScoreRequest request, User user) {
        Game game = gameRepository.findByIdAndFestivalSlug(gameId, slug)
                .orElseThrow(() -> new IllegalArgumentException("해당 게임이 존재하지 않습니다."));

        GameScore score = GameScore.builder()
                .game(game)
                .user(user.getRole().name().equals("USER") ? user : null)
                .festivalSlug(slug)
                .nickname(request.getNickname())
                .score(request.getScore())
                .createdAt(LocalDateTime.now())
                .build();

        gameScoreRepository.save(score);

        // slug별 방송
        String message = request.getNickname() + "님이 " + slug + " 게임을 클리어했습니다!";
        gameWebSocketHandler.broadcastToSlug(slug, message);
    }

    @Transactional(readOnly = true)
    public GameRankingResponse getRanking(String slug, Long gameId) {
        List<GameScore> scores = gameScoreRepository.findByFestivalSlugAndGame_IdOrderByScoreDesc(slug, gameId);

        List<GameScoreResponse> responseList = scores.stream()
                .map(gs -> new GameScoreResponse(gs.getNickname(), gs.getScore(),
                        gameScoreRepository.findRankByScore(gameId, slug, gs.getScore())))
                .collect(Collectors.toList());

        return new GameRankingResponse(slug, gameId, responseList);
    }

    @Transactional(readOnly = true)
    public GameScoreResponse getMyScore(String slug, Long gameId, String nickname) {
        GameScore score = gameScoreRepository.findTopByFestivalSlugAndNicknameOrderByScoreDesc(slug, nickname)
                .orElseThrow(() -> new IllegalArgumentException("점수가 없습니다."));
        int rank = gameScoreRepository.findRankByScore(gameId, slug, score.getScore());
        return new GameScoreResponse(nickname, score.getScore(), rank);
    }
}
