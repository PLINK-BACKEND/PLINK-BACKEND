package com.plink.backend.feed.controller.comment;

import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.service.comment.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/comments")
public class CommentLikeController {

    private final CommentLikeService CommentLikeService;

    // 댓글 좋아요 추가 & 취소
    @PostMapping("/{commentId}/like")
    public ResponseEntity<LikeResponse> commentLike(
            @PathVariable String slug,
            @AuthenticationPrincipal User user,
            @PathVariable Long commentId

    ){
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        LikeResponse response = CommentLikeService.Like(user,commentId,slug);
        return ResponseEntity.ok(response);
    }
}
