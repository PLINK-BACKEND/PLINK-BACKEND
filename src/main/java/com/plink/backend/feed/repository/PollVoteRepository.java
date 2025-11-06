package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    boolean existsByPollIdAndVoterUserId(Long pollId, Long voterId);
}
