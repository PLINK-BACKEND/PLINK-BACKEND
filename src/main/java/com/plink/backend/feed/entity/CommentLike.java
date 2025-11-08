package com.plink.backend.feed.entity;


import com.plink.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class CommentLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public CommentLike(User user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
}
