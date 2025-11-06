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
    private String content;
    private LocalDateTime createdAt;
    private int likeCount;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(comment.getAuthor().getNickname())
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount())
                .build();
    }
}
