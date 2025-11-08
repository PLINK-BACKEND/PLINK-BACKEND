package com.plink.backend.feed.dto.poll;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PollOptionResponse {
    private Long optionId;
    private String content;
    private int voteCount;
}
