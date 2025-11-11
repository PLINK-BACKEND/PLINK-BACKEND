package com.plink.backend.feed.repository.poll;

import com.plink.backend.feed.entity.poll.PollOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollOptionRepository extends JpaRepository<PollOption, Long> {
}
