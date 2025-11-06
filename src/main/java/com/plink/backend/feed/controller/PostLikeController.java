package com.plink.backend.feed.controller;

import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/post")
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 게시글 좋아요 추가 & 취소
    @PostMapping("{postId}/like")
    public ResponseEntity<LikeResponse> postLike(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal User userDetails
    ){
        User user = userDetails.getUser();
        LikeResponse response = postLikeService.Like(user, postId);
        return ResponseEntity.ok(response);
    }
}
