package com.plink.backend.feed.controller;

import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.PostCreateRequest;
import com.plink.backend.feed.dto.PostResponse;
import com.plink.backend.feed.dto.PostUpdateRequest;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.service.PostService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/post")
public class PostController {
    private final PostService postService;

    // 게시글 작성 (POST 요청)
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String slug,
            @AuthenticationPrincipal /* User */ Object author,
            @ModelAttribute PostCreateRequest request) throws IOException {

        User user = (User) author;
        Post post = postService.createPost(user, request);

        PostResponse response = PostResponse.from(post); // 엔티티 → DTO 변환

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // 게시글 수정 (PATCH)
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal Object author,
            @RequestBody PostUpdateRequest request) {
        User user = (User) author;
        Post updated = postService.updatePost(user, request, postId);
        PostResponse response = PostResponse.from(updated);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 삭제 (DELETE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal Object author) {
        User user = (User) author;
        postService.deletePost(user, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 상세 조회 (댓글까지 전부)
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(
            @PathVariable String slug, @PathVariable Long postId) {
        PostResponse response = postService.getPostDetail(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 전체 조회 (GET)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPostList(
            @PathVariable String slug,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> responses = postService.getPostList(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
