package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Poll;
import com.plink.backend.feed.entity.PollVote;
import com.plink.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
        boolean existsByPollAndVoter(Poll poll, User voter);
}
