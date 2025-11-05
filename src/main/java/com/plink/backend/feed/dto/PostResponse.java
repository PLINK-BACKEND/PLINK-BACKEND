package com.plink.backend.feed.dto;

import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostResponse {
    private Long id;
    private String postType;
    private String title;
    private String content;
    private String author;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;           // S3에서 변환된 URL
    private List<CommentResponseDto> comments; // 댓글 리스트

    // 엔티티 -> DTO 변환 편의 메서드
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())     // User 엔티티에 맞게 수정
                .tagName(post.getTag().getTag_name())
                .festivalName(post.getFestival().getName())     // Festival 엔티티에 맞게 수정
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(post.getImages() == null ? List.of() :
                        post.getImages().stream()
                                .map(Image::getS3key)
                                .collect(Collectors.toList()))
                .comments(post.getComments() == null ? List.of() :
                        post.getComments().stream()
                                .map(CommentResponseDto::from)
                                .collect(Collectors.toList()))
                .build();

    }


}
