package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
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

    // 전체 게시글 (slug 기준)
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug ORDER BY p.createdAt ASC")
    Slice<Post> findAllByFestivalSlugOrderByCreatedAtAsc(
            @Param("slug") String slug,
            Pageable pageable
    );

    // 태그 이름으로 필터링 (slug + tag_name)
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug AND p.tag.tag_name = :tagName ORDER BY p.createdAt ASC")
    Slice<Post> findAllByFestivalSlugAndTag_Tag_nameOrderByCreatedAtAsc(
            @Param("slug") String slug,
            @Param("tagName") String tagName,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "author.user",
            "tag",
            "images",
            "comments",
            "likes",
            "poll"
    })
    List<Post> findByAuthor_User_UserId(Long userId);

    // 숨김 제외 (slug 기준)
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug AND p.id NOT IN :ids ORDER BY p.createdAt ASC")
    Slice<Post> findAllByFestivalSlugAndIdNotInOrderByCreatedAtAsc(
            @Param("slug") String slug,
            @Param("ids") List<Long> ids,
            Pageable pageable
    );

    // 숨김 제외 + 태그 이름 필터링
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug AND p.tag.tag_name = :tagName AND p.id NOT IN :ids ORDER BY p.createdAt ASC")
    Slice<Post> findAllByFestivalSlugAndTag_Tag_nameAndIdNotInOrderByCreatedAtAsc(
            @Param("slug") String slug,
            @Param("tagName") String tagName,
            @Param("ids") List<Long> ids,
            Pageable pageable
    );

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
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug " +
            "ORDER BY (p.likeCount + p.commentCount) DESC, p.createdAt DESC")
    List<Post> findPopularPostsBySlug(@Param("slug") String slug, Pageable pageable);

    // 제목 또는 내용에 검색어가 포함
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Slice<Post> searchBySlugAndKeyword(@Param("slug") String slug,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);
    // 태그로 필터링 + 검색어
    @Query("SELECT p FROM Post p WHERE p.festival.slug = :slug " +
            "AND p.tag.tag_name = :tagName " +
            "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Slice<Post> searchBySlugAndTagAndKeyword(@Param("slug") String slug,
                                             @Param("tagName") String tagName,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);



}
