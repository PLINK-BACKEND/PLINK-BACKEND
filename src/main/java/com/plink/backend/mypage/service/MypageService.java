package com.plink.backend.mypage.service;

import com.plink.backend.feed.dto.poll.PollResponse;
import com.plink.backend.feed.dto.post.PostDetailResponse;
import com.plink.backend.feed.dto.post.PostResponse;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.PostLike;
import com.plink.backend.feed.entity.PostType;
import com.plink.backend.feed.repository.PostLikeRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.user.entity.UserFestival;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    // 내가 쓴 글 보기
    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(Long userId) {
        List<Post> posts = postRepository.findByAuthor_User_UserId(userId);
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }

    // 내가 좋아요 누른 글 보기
    @Transactional(readOnly = true)
    public List<PostResponse> getLikedPosts(Long userId) {
        List<PostLike> likes = postLikeRepository.findByUser_User_UserIdOrderByPost_CreatedAtDesc(userId);

        return likes.stream()
                .map(like -> PostResponse.from(like.getPost()))
                .toList();
    }

}

