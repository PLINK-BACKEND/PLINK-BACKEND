package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.PostLike;
import com.plink.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostLikeRepository extends JpaRepository<PostLike, Long>{

    boolean existsByPostAndUser(User user, Post post);
    void deleteByPostAndUser(User user, Post post);
    long countByPost(Post post);

}
