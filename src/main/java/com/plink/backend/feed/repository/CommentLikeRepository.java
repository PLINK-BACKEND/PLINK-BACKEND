package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.CommentLike;
import com.plink.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByCommentAndUser(User user, Comment comment);
    void deleteByCommentAndUser(User user, Comment comment);
    long countByComment(Comment comment);
}
