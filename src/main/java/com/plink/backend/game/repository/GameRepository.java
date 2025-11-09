package com.plink.backend.game.repository;

import com.plink.backend.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByIdAndFestivalSlug(Long id, String festivalSlug);
}
