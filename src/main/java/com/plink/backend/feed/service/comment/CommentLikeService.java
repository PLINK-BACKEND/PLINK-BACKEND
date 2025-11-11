package com.plink.backend.feed.service.comment;

import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.comment.CommentLike;
import com.plink.backend.feed.repository.comment.CommentLikeRepository;
import com.plink.backend.feed.repository.comment.CommentRepository;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserFestivalRepository UserFestivalRepository;

    @Transactional
    public LikeResponse Like(User user, Long commentId, String slug) {

        // 댓글 검증
        Comment comment = commentRepository.findById(commentId).
                orElseThrow(()-> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 로그인한 유저의 UserFestival 조회
        UserFestival userFestival = UserFestivalRepository
                .findByUser_UserIdAndFestivalSlug(user.getUserId(), slug)
                .orElseThrow(() -> new IllegalArgumentException("해당 축제에서 유저를 찾을 수 없습니다."));

        boolean liked;

        // 이미 좋아요를 한 경우 -> 좋아요 취소
        if (commentLikeRepository.existsByUserAndComment(userFestival,comment)) {
            commentLikeRepository.deleteByUserAndComment(userFestival,comment);
            comment.decreaseLikeCount();
            liked = false;
        } else{
            // 좋아요 추가
            commentLikeRepository.save(new CommentLike(userFestival, comment));
            comment.increaseLikeCount();
            liked = true;
        }

        long likeCount = commentLikeRepository.countByComment(comment);
        commentRepository.save(comment);
        return new LikeResponse(liked, likeCount);
    }


}

