package com.plink.backend.main.service;

import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getPopularPosts(String slug) {
        Pageable top3 = PageRequest.of(0, 3);
        List<Post> posts = postRepository.findPopularPostsBySlug(slug, top3);
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }

}
