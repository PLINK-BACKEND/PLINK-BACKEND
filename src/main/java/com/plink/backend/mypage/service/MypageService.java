package com.plink.backend.mypage.service;

import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(Long userId) {
        List<Post> posts = postRepository.findByAuthor_User_UserId(userId);
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }
}

