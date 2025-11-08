package com.plink.backend.mypage.controller;

import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.mypage.service.MypageService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/mypage")
public class MypageController {

    private final MypageService mypageService;

    // 내가 쓴 글 조회
    @GetMapping("/posts")
    public ResponseEntity<List<PostResponse>> getMyPosts(
            @PathVariable String slug,
            @AuthenticationPrincipal User user) {
        List<PostResponse> posts = mypageService.getMyPosts(user.getUserId());
        return ResponseEntity.ok(posts);
    }

    // 내가 좋아요 누른 글 조회
    @GetMapping("/posts/liked")
    public ResponseEntity<List<PostResponse>> getLikedPosts(
            @AuthenticationPrincipal User user
    ) {
        List<PostResponse> response = mypageService.getLikedPosts(user.getUserId());
        return ResponseEntity.ok(response);
    }


}
