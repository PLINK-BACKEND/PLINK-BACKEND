package com.plink.backend.feed.controller;

import com.plink.backend.feed.dto.PostRequest;
import com.plink.backend.feed.dto.PostResponse;
import com.plink.backend.feed.dto.PostUpdateRequest;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.service.PostService;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
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

    // 게시글 생성 (POST 요청)
    @PostMapping
    public ResponseEntity<PostResponse> createPost
    (@PathVariable String slug, @AuthenticationPrincipal /*User*/ Object currentUser,   // 여기 타입은 네 프로젝트에 맞게 변경 (예: CustomUserDetails, User 등)
     @ModelAttribute PostRequest requestDto) throws IOException {

        User author = (User) currentUser;
        Post post = postService.createPost(author,requestDto);
        PostResponse response = PostResponse.from(post);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // 게시글 수정 (PATCH)
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal  Object currentUser,
            @RequestBody PostUpdateRequest requestDto
    ){
        User author =  (User) currentUser;
        Post updated = postService.updatePost(postId,author,requestDto);
        PostResponse response = PostResponse.from(updated);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 삭제 (DELETE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal  Object currentUser
    ){
        User author = (User) currentUser;

        postService.deletePost(postId, author);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 상세 조회 (댓글까지 전부)
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPostDetail(
            @PathVariable String slug,@PathVariable Long postId) {
        PostResponse response = postService.getPostDetail(postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 전체 조회 (GET)
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPostList(
            @PathVariable String slug,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PostResponse> responses = postService.getPostList(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }
}
