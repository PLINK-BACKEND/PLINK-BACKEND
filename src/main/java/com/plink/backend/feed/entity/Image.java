package com.plink.backend.feed.entity;

import jakarta.persistence.*;

@Entity
public class Image {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="post_id")
    private Post post;

    private String s3key;
    private String originalname;
    private String image_url;
}
