package com.plink.backend.feed.repository.comment;

import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.post.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    @EntityGraph(attributePaths = {"post", "post.author.user", "post.tag"})
    List<Comment> findByAuthor_User_UserIdOrderByCreatedAtAsc(Long userId);

}
