package com.plink.backend.feed.repository.comment;

import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.post.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    @EntityGraph(attributePaths = {"post", "post.author.user", "post.tag"})
    List<Comment> findByAuthor_User_UserIdOrderByCreatedAtAsc(Long userId);

    @Query("""
    SELECT DISTINCT p FROM Comment c
    JOIN c.post p
    WHERE c.author.user.userId = :userId
      AND (:keyword IS NULL
           OR p.title LIKE %:keyword%
           OR p.content LIKE %:keyword%)
    ORDER BY p.createdAt DESC
    """)
    List<Post> searchMyCommentedPosts(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );

}
