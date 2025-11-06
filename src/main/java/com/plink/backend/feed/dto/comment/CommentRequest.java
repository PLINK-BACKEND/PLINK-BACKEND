package com.plink.backend.feed.dto.comment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CommentRequest {
    private String content;
}
