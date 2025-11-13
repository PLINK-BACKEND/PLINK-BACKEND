package com.plink.backend.feed.repository.post;

import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.entity.post.PostLike;
import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long>{

    boolean existsByUserAndPost(UserFestival user, Post post);
    void deleteByUserAndPost(UserFestival user, Post post);
    long countByPost(Post post);

    @EntityGraph(attributePaths = {
            "post.author.user",
            "post.tag",
            "post.images",
            "post.comments",
            "post.likes",
            "post.poll"
    })
    List<PostLike> findByUser_User_UserIdOrderByPost_CreatedAtDesc(Long userId);

    // 검색
    @Query("""
    SELECT p FROM PostLike pl
    JOIN pl.post p
    WHERE pl.user.user.userId = :userId
      AND (:keyword IS NULL 
           OR p.title LIKE %:keyword%
           OR p.content LIKE %:keyword%)
    ORDER BY p.createdAt DESC
    """)
    List<Post> searchMyLikedPosts(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );
}

