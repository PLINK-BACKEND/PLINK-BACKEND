package com.plink.backend.festival.repository;

import com.plink.backend.festival.entity.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    Optional<Festival> findBySlug(String slug);

    // 축제명 또는 slug 검색
    @Query("""
        SELECT f FROM Festival f
        WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(f.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY f.startDate DESC
    """)
    List<Festival> searchByKeyword(@Param("keyword") String keyword);
    List<Festival> findByNameContainingIgnoreCase(String keyword);
}
