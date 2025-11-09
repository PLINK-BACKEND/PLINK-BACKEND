package com.plink.backend.feed.dto.post;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.entity.Image;
import com.plink.backend.feed.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
// 댓글리스트 제외
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {
    private Long id;
    private String postType;
    private String title;
    private String content;
    private String author;
    private String profileImageUrl;
    private String tagName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageInfo> images;
    private int commentCount;
    private int likeCount;
    private PollResponse poll;


    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .postType(post.getPostType().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .author(post.getAuthor().getNickname())
                .profileImageUrl(post.getAuthor().getUser().getProfileImageUrl())
                .tagName(post.getTag().getTag_name())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .images(post.getImages() == null ? List.of() :
                        post.getImages().stream()
                                .map(img -> new PostResponse.ImageInfo(img.getId(), img.getImageUrl()))
                                .collect(Collectors.toList()))
                .commentCount(post.getCommentCount())
                .likeCount(post.getLikeCount())
                .poll(post.getPoll() == null ? null : PollResponse.from(post.getPoll(), null))
                .build();
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImageInfo {
        private Long id;
        private String imageUrl;
    }

    @Getter
    @Builder
    public static class SliceResult {
        private List<PostResponse> posts;
        private boolean hasNext;
        private int page;
        private int size;

        public static SliceResult from(Slice<PostResponse> slice) {
            return SliceResult.builder()
                    .posts(slice.getContent())
                    .hasNext(slice.hasNext())
                    .page(slice.getNumber())
                    .size(slice.getSize())
                    .build();
        }
    }
}

