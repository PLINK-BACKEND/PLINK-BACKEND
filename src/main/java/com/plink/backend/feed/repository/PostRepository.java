package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"images"})
    Optional<Post> findById(Long id);

    // 게시판 분리
    Page<Post> findAllByTag_IdOrderByCreatedAtAsc(Long tagId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "author.user",      // UserFestival → User
            "tag",              // 게시글 태그
            "images",           // 게시글 이미지
            "comments",         // 댓글
            "likes",            // 좋아요
            "poll"              // 설문(앙케이트)
    })
    List<Post> findByAuthor_User_UserId(Long userId);

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

    // 인기글
    @Query("SELECT p FROM Post p " +
            "ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    List<Post> findTop3PopularPosts(Pageable pageable);

}