package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
