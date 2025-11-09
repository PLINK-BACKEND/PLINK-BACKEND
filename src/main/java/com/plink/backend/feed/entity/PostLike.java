package com.plink.backend.feed.entity;

import com.plink.backend.feed.entity.Post;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
public class PostLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_festival_id", nullable = false)
    private UserFestival user;

    public PostLike(UserFestival user, Post post) {
        this.user = user;
        this.post = post;
    }
}
