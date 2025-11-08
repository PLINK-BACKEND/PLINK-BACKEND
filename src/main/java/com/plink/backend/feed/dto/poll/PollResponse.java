package com.plink.backend.feed.dto.poll;

import com.plink.backend.feed.entity.Poll;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder

public class PollResponse {
    private Long pollId;
    private Long selectedOptionId;
    private List<PollOptionResponse> result;
    private LocalDateTime createdAt;

    public static PollResponse from(Poll poll, Long selectedOptionId) {
        return PollResponse.builder()
                .pollId(poll.getId())
                .selectedOptionId(selectedOptionId)
                .result(
                        poll.getOptions().stream()
                                .map(option -> PollOptionResponse.builder()
                                        .optionId(option.getId())
                                        .content(option.getContent())
                                        .voteCount(option.getVoteCount())
                                        .build()
                                )
                                .toList()
                )
                .createdAt(LocalDateTime.now())
                .build();
    }
}
