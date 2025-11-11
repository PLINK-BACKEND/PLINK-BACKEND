package com.plink.backend.feed.dto.poll;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.plink.backend.feed.entity.poll.Poll;
import com.plink.backend.feed.entity.poll.PollOption;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PollResponse {
    private Long pollId;
    private Long selectedOptionId;
    private List<PollOptionResponse> result;

    public static PollResponse from(Poll poll, Long selectedOptionId) {

        int totalVotes = poll.getOptions().stream()
                .mapToInt(PollOption::getVoteCount)
                .sum();

        return PollResponse.builder()
                .pollId(poll.getId())
                .selectedOptionId(selectedOptionId)
                .result(
                        poll.getOptions().stream()
                                .map(option -> PollOptionResponse.builder()
                                        .optionId(option.getId())
                                        .content(option.getContent())
                                        .voteCount(option.getVoteCount())
                                        .voteRate(totalVotes == 0 ? 0:
                                                (int) Math.round((option.getVoteCount() * 100.0) / totalVotes))
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}
