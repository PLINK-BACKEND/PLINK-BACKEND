package com.plink.backend.feed.service.post;


import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.feed.entity.post.PostLike;
import com.plink.backend.feed.repository.post.PostLikeRepository;
import com.plink.backend.feed.repository.post.PostRepository;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    @Transactional
    public LikeResponse Like(User user, Long postId, String slug) {

        // 게시글 검증
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND,"존재하지 않는 게시글입니다."));

        // 작성자-축제 매핑 검증
        UserFestival userFestival = postService.getVerifiedUserFestival(user, slug);

        boolean liked;

        // 이미 좋아요한 경우 → 취소
        if (postLikeRepository.existsByUserAndPost(userFestival, post)) {
            postLikeRepository.deleteByUserAndPost(userFestival, post);
            post.decreaseLikeCount();
            liked = false;
        }
        // 좋아요 추가
        else {
            postLikeRepository.save(new PostLike(userFestival, post));
            post.increaseLikeCount();
            liked = true;
        }

        long likeCount = postLikeRepository.countByPost(post);
        postRepository.save(post);
        return new LikeResponse(liked, likeCount);

    }
}
