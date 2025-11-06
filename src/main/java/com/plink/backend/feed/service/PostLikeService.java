package com.plink.backend.feed.service;


import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.entity.PostLike;
import com.plink.backend.feed.repository.PostLikeRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public LikeResponse Like(User user, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        boolean liked;

        // 이미 좋아요를 한 경우 -> 좋아요 취소
        if (postLikeRepository.existsByPostAndUser(user,post)) {
            postLikeRepository.deleteByPostAndUser(user,post);
            post.decreaseLikeCount();
            liked = false;
        } else{
            // 좋아요 추가
            postLikeRepository.save(new PostLike(user,post));
            post.increaseLikeCount();
            liked = true;
        }

        long likeCount = postLikeRepository.countByPost(post);
        return new LikeResponse(liked, likeCount);
    }
}
