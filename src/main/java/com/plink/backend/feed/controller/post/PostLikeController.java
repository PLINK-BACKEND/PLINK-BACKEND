package com.plink.backend.feed.controller.post;

import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.service.post.PostLikeService;
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
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> postLike(
            @PathVariable String slug,
            @AuthenticationPrincipal User user,
            @PathVariable Long postId

    ){
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        LikeResponse response = postLikeService.Like(user, postId, slug);
        return ResponseEntity.ok(response);
    }
}
