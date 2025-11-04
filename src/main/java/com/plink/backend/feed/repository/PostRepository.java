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
    @EntityGraph(attributePaths = {"author", "comments", "images"})
    Optional<Post> findWithAllById(Long id);
    Page<Post> findAllByOrderByCreatedAtAsc(Pageable pageable);
}
