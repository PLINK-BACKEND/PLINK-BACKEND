package com.plink.backend.feed.dto.post;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Poll;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.user.entity.User;
import io.micrometer.common.lang.Nullable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDetailResponse {
    private Long id;
    private String postType;
    private String title;
    private String content;
    private String author;
    private String profileImageUrl;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageInfo> images;          // S3에서 변환된 URL
    private List<CommentResponse> comments; // 댓글 리스트
    private List<Long> hiddenCommentIds;
    private int commentCount;
    private int likeCount;


    @Nullable
    private PollResponse poll;

    // 게시글 상세보기

    public static PostDetailResponse from(Post post) {
        return from(post, List.of());
    }
    public static PostDetailResponse from(Post post, List<Long> hiddenCommentIds) {
        List<CommentResponse> visibleComments = post.getComments().stream()
                .filter(comment -> !hiddenCommentIds.contains(comment.getId()))
                .map(CommentResponse::from)
                .collect(Collectors.toList());

        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())
                .profileImageUrl(post.getAuthor().getUser().getProfileImageUrl())
                .tagName(post.getTag().getTag_name())
                .postType(post.getPostType().toString())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .images(post.getImages() == null ? List.of() :
                        post.getImages().stream()
                                .map(img -> new ImageInfo(img.getId(), img.getImageUrl()))
                                .collect(Collectors.toList()))
                .comments(visibleComments)
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .build();

    }


    public static PostDetailResponse from(Post post, PollResponse pollResponse) {
        return from(post, pollResponse, List.of()); // 댓글 필터링 없음 → 전체 표시
    }

    // 숨긴 댓글 필터링용 버전
    public static PostDetailResponse from(Post post, PollResponse pollResponse, List<Long> hiddenCommentIds) {

        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())
                .tagName(post.getTag().getTag_name())
                .poll(pollResponse)
                .createdAt(post.getCreatedAt())
                .images(post.getImages() == null ? List.of() :
                        post.getImages().stream()
                                .map(img -> new ImageInfo(img.getId(), img.getImageUrl()))
                                .collect(Collectors.toList()))
                .comments(
                        post.getComments() == null ? List.of() :
                                post.getComments().stream()
                                        .filter(c -> hiddenCommentIds == null || !hiddenCommentIds.contains(c.getId()))
                                        .map(CommentResponse::from)
                                        .collect(Collectors.toList())

                )
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .build();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageInfo {
        private Long id;
        private String imageUrl;
    }
}
