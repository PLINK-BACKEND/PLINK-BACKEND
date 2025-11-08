package com.plink.backend.feed.service;

import com.plink.backend.user.service.UserService;
import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.poll.PollCreateRequest;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.entity.Poll;
import com.plink.backend.feed.entity.PollOption;
import com.plink.backend.feed.repository.PollOptionRepository;
import com.plink.backend.feed.repository.PollRepository;
import com.plink.backend.feed.repository.PollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PollService {
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final UserService userService;

    // 앙케이트 생성
    @Transactional
    public Poll createPoll(User author, PollCreateRequest request) {

        // 투표 생성
        Poll poll = Poll.builder()
                .title(request.getQuestion())
                .build();

        // 옵션 생성
        for (String content : request.getOptions()){
            PollOption option = PollOption.builder()
                    .content(content)
                    .voteCount(0)
                    .build();
            poll.addOption(option);
        }

        // 저장
        return pollRepository.save(poll);

    }

    // 앙케이트 투표
    @Transactional
    public PollResponse vote(Long pollId, Long optionId, Long voterId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new IllegalArgumentException("투표를 찾을 수 없습니다."));

        PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException("선택지를 찾을 수 없습니다."));

        if (!option.getPoll().getId().equals(pollId)) {
            throw new IllegalArgumentException("선택지와 투표가 일치하지 않습니다.");
        }

        // 중복투표 체크
        if (pollVoteRepository.existsByPollIdAndVoterUserId(pollId, voterId)) {
            throw new IllegalStateException("이미 투표했습니다.");
        }

        User voter = userService.getById(voterId);

        // 집계
        option.setVoteCount(option.getVoteCount() + 1);

        // 저장
        pollOptionRepository.save(option);

        return PollResponse.from(poll, optionId);
    }


}
