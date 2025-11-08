package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor

public class Image {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String s3key;
    private String originalName;
    private String image_url;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="post_id")
    private Post post;

    public String getS3key() { return s3key; }
}
