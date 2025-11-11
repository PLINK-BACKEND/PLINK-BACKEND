package com.plink.backend.feed.controller.post;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.report.ReportTargetType;
import com.plink.backend.feed.repository.report.HiddenContentRepository;
import com.plink.backend.user.entity.User;
import com.plink.backend.feed.dto.post.PostCreateRequest;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.dto.post.PostUpdateRequest;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.service.post.PostService;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/posts")
public class PostController {
    private final PostService postService;
    private final HiddenContentRepository hiddenContentRepository;
    private final UserFestivalRepository userFestivalRepository;

    // 게시글 작성 (POST 요청)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponse> createPost(
            @PathVariable String slug,
            @AuthenticationPrincipal User user,
            @ModelAttribute PostCreateRequest request) throws IOException {

        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Post post = postService.createPost(user, request,slug);
        PostResponse response = PostResponse.from(post); // 엔티티 → DTO 변환

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    // 게시글 수정 (PATCH)
    @PatchMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> updatePost(
            @PathVariable String slug,
            @PathVariable Long postId,
            @ModelAttribute PostUpdateRequest request) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        User user = (User) authentication.getPrincipal();

        Post updated = postService.updatePost(user, request, postId);

        List<Long> hiddenCommentIds = List.of();
        if (user != null) {
            UserFestival userFestival = userFestivalRepository
                    .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                    .orElse(null);

            if (userFestival != null) {
                hiddenCommentIds = hiddenContentRepository
                        .findTargetIdsByUserFestivalAndTargetType(userFestival, ReportTargetType.COMMENT);
            }
        }

        PostDetailResponse response = PostDetailResponse.from(updated, hiddenCommentIds);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // 게시글 삭제 (DELETE)
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String slug,
            @PathVariable Long postId)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }

        User user = (User) authentication.getPrincipal();
        postService.deletePost(user, postId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 게시글 상세 조회 (댓글까지 전부)
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable String slug,
            @PathVariable Long postId,
            @AuthenticationPrincipal User user) {
        PostDetailResponse response = postService.getPostDetail(user, slug, postId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }



    // 게시판별 전체 조회 + 검색
    @GetMapping
    public ResponseEntity<PostResponse.SliceResult> getPostListByTag(
            @AuthenticationPrincipal User user,
            @PathVariable String slug,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        PostResponse.SliceResult result = postService.getPostListByTag(user, slug, pageable, tag, keyword);
        return ResponseEntity.ok(result);

    }


}
