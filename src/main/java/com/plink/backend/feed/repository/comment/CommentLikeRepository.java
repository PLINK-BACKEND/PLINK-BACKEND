package com.plink.backend.feed.repository.comment;

import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.comment.CommentLike;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserAndComment(UserFestival user, Comment comment);
    void deleteByUserAndComment(UserFestival user, Comment comment);
    long countByComment(Comment comment);
}
