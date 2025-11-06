package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findById(Long id);

    // 상세 조회: author, tag, festival, images, comments까지 한 번에 로딩
    @EntityGraph(attributePaths = {
            "author",
            "tag",
            "festival",
            "images",
            "comments",
            "comments.author"
    })
    Optional<Post> findWithAllById(Long id);

    // 목록 조회
    Page<Post> findAllByOrderByCreatedAtAsc(Pageable pageable);
}
