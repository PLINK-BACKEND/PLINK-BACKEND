package com.plink.backend.feed.dto.post;


import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
// 댓글리스트 제외
public class PostResponse {
    private Long id;
    private String title;
    private String author;
    private String profileImageUrl;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private int commentCount;
    private int likeCount;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .author(post.getAuthor().getNickname())
                .profileImageUrl(post.getAuthor().getProfileImageUrl())
                .tagName(post.getTag().getTag_name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(post.getImages() == null ? List.of() :
                        post.getImages().stream()
                                .map(Image::getImage_url)
                                .collect(Collectors.toList()))
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .build();
    }
}
