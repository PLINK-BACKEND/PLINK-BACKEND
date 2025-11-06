package com.plink.backend.feed.controller;

import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("{slug}/comment")
public class CommentLikeController {

    private final CommentLikeService CommentLikeService;

    // 댓글 좋아요 추가 & 취소
    @PostMapping("/{commentId}/like")
    public ResponseEntity<LikeResponse> commentLike(
            @PathVariable String slug,
            @PathVariable Long commentId,
            @AuthenticationPrincipal User userDetails
    ){
        User user = userDetails.getUser();
        LikeResponse response = CommentLikeService.Like(user,commentId);
        return ResponseEntity.ok(response);
    }
}
