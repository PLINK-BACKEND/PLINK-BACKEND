package com.plink.backend.feed.dto.post;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PostUpdateRequest {
    private String title;
    private String content;
    private Long tagId;
}
