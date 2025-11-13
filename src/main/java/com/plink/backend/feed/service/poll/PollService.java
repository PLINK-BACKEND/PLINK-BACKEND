package com.plink.backend.feed.service.poll;

import com.plink.backend.feed.entity.poll.PollVote;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.service.UserService;
import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.poll.PollCreateRequest;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.entity.poll.Poll;
import com.plink.backend.feed.entity.poll.PollOption;
import com.plink.backend.feed.repository.poll.PollOptionRepository;
import com.plink.backend.feed.repository.poll.PollRepository;
import com.plink.backend.feed.repository.poll.PollVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "투표를 찾을 수 없습니다."));


        PollOption option = pollOptionRepository.findById(optionId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "선택지를 찾을 수 없습니다."));
        if (!option.getPoll().getId().equals(pollId)) {
            throw new  CustomException(HttpStatus.BAD_REQUEST, "선택지와 투표가 일치하지 않습니다.");
        }

        User voter = userService.getById(voterId);

        // 중복투표 체크
        if (pollVoteRepository.existsByPollAndVoter(poll, voter)) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 투표했습니다.");
        }

        // 집계
        option.increaseVoteCount();

        PollVote vote = PollVote.builder()
                .poll(poll)
                .voter(voter)
                .option(option)
                .build();

        // 저장
        pollVoteRepository.save(vote);
        Long selectedOptionId = option.getId();

        return PollResponse.from(poll, selectedOptionId);
    }

    @Transactional(readOnly = true)
    public PollResponse getPollResponse(Poll poll, User user) {
        Long selectedOptionId = null;

        if (user != null) {
            selectedOptionId = pollVoteRepository.findByPollAndVoter(poll, user)
                    .map(vote -> vote.getOption().getId())
                    .orElse(null);
        }

        return PollResponse.from(poll, selectedOptionId);
    }

}
