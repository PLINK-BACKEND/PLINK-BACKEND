package com.plink.backend.game.repository;

import com.plink.backend.game.entity.GameScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface GameScoreRepository extends JpaRepository<GameScore, Long> {

    List<GameScore> findByFestivalSlugAndGame_IdOrderByScoreDesc(String slug, Long gameId);

    Optional<GameScore> findTopByFestivalSlugAndNicknameOrderByScoreDesc(String slug, String nickname);

    @Query("SELECT COUNT(gs) + 1 FROM GameScore gs WHERE gs.game.id = :gameId AND gs.festivalSlug = :slug AND gs.score > :score")
    int findRankByScore(Long gameId, String slug, int score);
}
