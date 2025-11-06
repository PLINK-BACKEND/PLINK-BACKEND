package com.plink.backend.feed.entity;


import jakarta.persistence.*;

@Entity
public class PostLike {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
