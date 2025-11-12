package com.plink.backend.feed.repository.poll;

import com.plink.backend.feed.entity.poll.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {
}
