package com.plink.backend.feed.entity.post;

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

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="post_id")
    private Post post;

    public String getS3key() { return s3key; }
}
