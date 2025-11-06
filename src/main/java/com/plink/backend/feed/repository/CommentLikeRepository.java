package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLikeRepository, Long> {

    boolean existsByCommentAndUser(User user, Comment comment);
    void deleteByCommentAndUser(User user, Comment comment);
    long countByComment(Comment comment);
}
