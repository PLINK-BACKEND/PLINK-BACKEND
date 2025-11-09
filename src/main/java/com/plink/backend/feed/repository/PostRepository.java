package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"images"})
    Optional<Post> findById(Long id);

    // 게시판 분리
    Page<Post> findAllByTag_IdOrderByCreatedAtAsc(Long tagId, Pageable pageable);

    // 숨김 제외
    Page<Post> findAllByIdNotInOrderByCreatedAtAsc(List<Long> ids, Pageable pageable);
    Page<Post> findAllByTag_IdAndIdNotInOrderByCreatedAtAsc(Long tagId, List<Long> ids, Pageable pageable);

    // 상세 조회: author, tag, festival, images, comments까지 한 번에 로딩
    @EntityGraph(attributePaths = {
            "author",
            "tag",
            "images",
            "comments",
            "comments.author"
    })
    Optional<Post> findWithAllById(Long id);

    // 목록 조회: 댓글은 필요 없음 → Lazy 그대로 두기
    @EntityGraph(attributePaths = {"author", "tag", "images"})
    Page<Post> findAllByOrderByCreatedAtAsc(Pageable pageable);


}