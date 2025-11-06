package com.plink.backend.feed.entity;


import jakarta.persistence.*;

@Entity
public class CommentLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
