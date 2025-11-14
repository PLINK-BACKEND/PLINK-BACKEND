package com.plink.backend.game.service;

import com.plink.backend.game.dto.GameRankingResponse;
import com.plink.backend.game.dto.GameScoreRequest;
import com.plink.backend.game.dto.GameScoreResponse;
import com.plink.backend.game.entity.Game;
import com.plink.backend.game.entity.GameScore;
import com.plink.backend.game.repository.GameRepository;
import com.plink.backend.game.repository.GameScoreRepository;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameScoreRepository gameScoreRepository;
    private final UserFestivalRepository userFestivalRepository;

    public void submitScore(String slug, Long gameId, GameScoreRequest request, User user) {
        // 게임 존재 여부 확인
        Game game = gameRepository.findByIdAndFestivalSlug(gameId, slug)
                .orElseThrow(() -> new IllegalArgumentException("해당 게임이 존재하지 않습니다."));

        boolean success = request.isSuccess();

        // 닉네임 결정 로직
        String nickname;
        String role;
        UserFestival uf = null;

        if (user != null && user.getRole() != null && user.getRole().name().equals("USER")) {
            uf = userFestivalRepository.findByUserAndFestivalSlug(user, slug)
                    .orElseThrow(() -> new IllegalArgumentException("해당 행사에 등록된 유저 닉네임이 없습니다."));
            nickname = uf.getNickname();
            role = "USER";
        } else {
            nickname = request.getNickname();
            role = "GUEST";
        }

        // 점수 저장
        GameScore score = GameScore.builder()
                .game(game)
                .nickname(nickname)
                .score(request.getScore())
                .festivalSlug(slug)
                .role(role)
                .build();

        gameScoreRepository.save(score);

        // Secret Frame 해금 로직
        if (uf != null && request.isSuccess()) {
            // 성공이면 무조건 해금 (false→true, true→true)
            if (!uf.isSecretFrameUnlocked()) {
                uf.setSecretFrameUnlocked(true);
                userFestivalRepository.save(uf);
            }
        }
    }

    @Transactional(readOnly = true)
    public GameRankingResponse getRanking(String slug, Long gameId) {
        List<GameScore> scores = gameScoreRepository.findByFestivalSlugAndGame_IdOrderByScoreDesc(slug, gameId);

        List<GameScoreResponse> responseList = scores.stream()
                .map(gs -> new GameScoreResponse(
                        gs.getNickname(),
                        gs.getScore(),
                        gameScoreRepository.findRankByScore(gameId, slug, gs.getScore())
                ))
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
