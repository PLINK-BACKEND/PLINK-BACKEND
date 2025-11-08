package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Poll;
import com.plink.backend.feed.entity.PollVote;
import com.plink.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
        boolean existsByPollAndVoter(Poll poll, User voter);
        Optional<PollVote> findByPollAndVoter(Poll poll, User voter);
}
