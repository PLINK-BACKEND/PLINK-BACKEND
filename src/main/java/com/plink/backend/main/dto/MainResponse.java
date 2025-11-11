package com.plink.backend.main.dto;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.post.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@AllArgsConstructor

public class MainResponse {
    private PostResponse popularPoll;
    private List<PostResponse> popularPosts;

    public static MainResponse from(Post popularPoll, List<Post> popularPosts) {
        return MainResponse.builder()
                .popularPoll(popularPoll == null ? null : PostResponse.from(popularPoll))
                .popularPosts(popularPosts == null ? List.of() :
                        popularPosts.stream()
                                .map(PostResponse::from)
                                .collect(Collectors.toList()))
                .build();
    }
}
