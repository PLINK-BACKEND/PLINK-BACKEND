package com.plink.backend.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {
    private boolean liked;
    private Long likeCount;
}
