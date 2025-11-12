package com.plink.backend.feed.entity.comment;


import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class CommentLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private UserFestival user;

    public CommentLike(UserFestival user, Comment comment) {
        this.user = user;
        this.comment = comment;
    }
}
