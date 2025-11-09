package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"images"})
    Optional<Post> findById(Long id);

    // 전체 게시글
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    Slice<Post> findAllByOrderByCreatedAtAsc(Pageable pageable);

    // 태그별 게시글
    @Query("SELECT p FROM Post p WHERE p.tag.id = :tagId ORDER BY p.createdAt DESC")
    Slice<Post> findAllByTag_IdOrderByCreatedAtAsc(@Param("tagId") Long tagId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "author.user",  // UserFestival → User
            "tag",
            "images",
            "comments",
            "likes",
            "poll"
    })
    List<Post> findByAuthor_User_UserId(Long userId);

    // 숨김 제외
    @Query("SELECT p FROM Post p WHERE p.id NOT IN :ids ORDER BY p.createdAt DESC")
    Slice<Post> findAllByIdNotInOrderByCreatedAtAsc(@Param("ids") List<Long> ids, Pageable pageable);

    // 숨김 제외 + 태그별 게시글
    @Query("SELECT p FROM Post p WHERE p.tag.id = :tagId AND p.id NOT IN :ids ORDER BY p.createdAt DESC")
    Slice<Post> findAllByTag_IdAndIdNotInOrderByCreatedAtAsc(@Param("tagId") Long tagId,
                                                             @Param("ids") List<Long> ids,
                                                             Pageable pageable);

    // 상세 조회
    @EntityGraph(attributePaths = {
            "author",
            "tag",
            "images",
            "comments",
            "comments.author"
    })
    Optional<Post> findWithAllById(Long id);

    // 인기글
    @Query("SELECT p FROM Post p " +
            "ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    List<Post> findTop3PopularPosts(Pageable pageable);
}
