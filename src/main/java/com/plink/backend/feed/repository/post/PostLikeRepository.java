package com.plink.backend.feed.repository.post;

import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.entity.post.PostLike;
import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

