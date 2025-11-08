package com.plink.backend.feed.controller;

import com.plink.backend.feed.service.PollService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/poll")
public class PollController {

    private final PollService pollService;

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<String> vote(
            @PathVariable Long pollId,
            @PathVariable Long optionId,
            @AuthenticationPrincipal User voter
    ) {
        pollService.vote(pollId,optionId, voter.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Vote succeed");
    }
}
