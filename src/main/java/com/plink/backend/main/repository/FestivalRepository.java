package com.plink.backend.main.repository;

import com.plink.backend.main.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Optional<Festival> findBySlug(String slug);
}
