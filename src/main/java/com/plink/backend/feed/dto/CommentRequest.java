package com.plink.backend.feed.dto;

import jakarta.persistence.GeneratedValue;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    private String content;
}
