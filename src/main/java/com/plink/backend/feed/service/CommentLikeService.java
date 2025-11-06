package com.plink.backend.feed.service;

import com.plink.backend.feed.dto.LikeResponse;
import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.CommentLike;
import com.plink.backend.feed.entity.PostLike;
import com.plink.backend.feed.repository.CommentLikeRepository;
import com.plink.backend.feed.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public LikeResponse Like(User user, Long commentId) {

        Comment comment = commentRepository.findById(commentId).
                orElseThrow(()-> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        boolean liked;

        // 이미 좋아요를 한 경우 -> 좋아요 취소
        if (commentLikeRepository.existsByCommentAndUser(user,comment)) {
            commentLikeRepository.deleteByCommentAndUser(user,comment);
            comment.decreaseLikeCount();
            liked = false;
        } else{
            // 좋아요 추가
            commentLikeRepository.save(new CommentLike(user, comment));
            comment.increaseLikeCount();
            liked = true;
        }

        long likeCount = commentLikeRepository.countByComment(comment);
        return new LikeResponse(liked, likeCount);
    }

    }

}

