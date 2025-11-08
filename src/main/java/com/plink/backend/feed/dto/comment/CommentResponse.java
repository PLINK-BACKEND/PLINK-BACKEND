package com.plink.backend.feed.dto.comment;


import com.plink.backend.feed.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor

public class CommentResponse {
    private Long id;
    private String author;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private String postTitle;
    private Long postId;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getNickname())
                .profileImageUrl(comment.getAuthor().getUser().getProfileImageUrl())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .likeCount(comment.getLikeCount())
                .postTitle(comment.getPost().getTitle())
                .postId(comment.getPost().getId())
                .build();
    }
}
