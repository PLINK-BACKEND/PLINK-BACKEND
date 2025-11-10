package com.plink.backend.mypage.controller;

import com.plink.backend.auth.dto.UserResponse;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.mypage.service.MypageService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    // 내가 작성한 댓글 보기
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> getMyComments(
            @AuthenticationPrincipal User user
    ) {
        List<CommentResponse> response = mypageService.getMyComments(user.getUserId());
        return ResponseEntity.ok(response);
    }

    // 닉네임 변경하기
    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @PathVariable String slug,
            @RequestPart(required = false) String nickname,
            @RequestPart(required = false) MultipartFile profileImage,
            @RequestPart(required = false) String defaultProfileUrl
    ) throws IOException {
        UserResponse response = mypageService.updateProfile(user, slug, nickname, profileImage ,defaultProfileUrl);
        return ResponseEntity.ok(response);
    }

    // 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String currentPassword,
            @RequestParam String newPassword
    ) {
        mypageService.changePassword(user, currentPassword, newPassword);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }


}
