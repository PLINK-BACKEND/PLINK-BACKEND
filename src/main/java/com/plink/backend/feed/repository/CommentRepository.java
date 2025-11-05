package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
}
