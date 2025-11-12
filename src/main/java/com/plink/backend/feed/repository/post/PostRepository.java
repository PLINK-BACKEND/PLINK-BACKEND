package com.plink.backend.feed.repository.post;

import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.entity.post.PostType;
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

    // 전체 게시글 (slug + tag_name + keyword + hiddenId 기준)
    @Query("""
    SELECT p
    FROM Post p
    WHERE p.festival.slug = :slug
      AND (:tagName IS NULL OR p.tag.tag_name = :tagName)
      AND (
            :keyword IS NULL OR
            LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      AND (COALESCE(:hiddenIds, NULL) IS NULL OR p.id NOT IN :hiddenIds)
    ORDER BY p.createdAt ASC 
""")
    Slice<Post> findPostsFiltered(
            @Param("slug") String slug,
            @Param("tagName") String tagName,
            @Param("keyword") String keyword,
            @Param("hiddenIds") List<Long> hiddenIds,
            Pageable pageable
    );

    // 내가 쓴 글 보기
    @EntityGraph(attributePaths = {
            "author.user",
            "tag",
            "images",
            "comments",
            "likes",
            "poll"
    })
    List<Post> findByAuthor_User_UserId(Long userId);

    // 상세 조회
    @EntityGraph(attributePaths = {
            "author",
            "tag",
            "images",
            "comments",
            "comments.author",
            "poll",
            "poll.options"
    })
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findWithAllById(@Param("id") Long id);

    // 인기글
    @Query("""
    SELECT p FROM Post p
    WHERE p.festival.slug = :slug
      AND (:postType IS NULL OR p.postType = :postType)
      AND (COALESCE(:hiddenIds, NULL) IS NULL OR p.id NOT IN :hiddenIds)
    ORDER BY (p.likeCount + p.commentCount) DESC , p.createdAt DESC 
""")
    List<Post> findPopularPosts(
            @Param("slug") String slug,
            @Param("postType") PostType postType,
            @Param("hiddenIds") List<Long> hiddenIds,
            Pageable pageable
    );
}
