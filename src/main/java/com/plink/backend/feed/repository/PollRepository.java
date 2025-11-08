package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Long> {
}
