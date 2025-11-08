package com.plink.backend.feed.controller;

import com.plink.backend.feed.dto.comment.CommentRequest;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.service.CommentService;
import com.plink.backend.user.entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/post")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @RequestBody CommentRequest request
    ) {
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Comment comment = commentService.createComment(user, request,postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    // 댓글 수정
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String slug,
            @PathVariable Long commentId,
            @RequestBody CommentRequest request
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        User user = (User) authentication.getPrincipal();

        Comment updated = commentService.updateComment(user, request,commentId);

        return ResponseEntity.ok(CommentResponse.from(updated));
    }

    // 게시글별 댓글 조회
    @GetMapping("/{postId}/comment")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String slug,
            @PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }


    // 댓글 삭제
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String slug,
            @PathVariable Long commentId)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        User user = (User) authentication.getPrincipal();

        commentService.deleteComment(user,commentId);
        return ResponseEntity.noContent().build();
    }
}
