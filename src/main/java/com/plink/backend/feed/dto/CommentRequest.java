package com.plink.backend.feed.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CommentRequest {
    private String content;
}
