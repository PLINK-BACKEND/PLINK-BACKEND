package com.plink.backend.feed.service;

import com.plink.backend.feed.dto.comment.CommentRequest;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.feed.repository.CommentRepository;
import com.plink.backend.feed.repository.PostRepository;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.entity.UserFestival;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserFestivalRepository userFestivalRepository;

    // 댓글 작성하기
    @Transactional
    public Comment createComment(User author, CommentRequest request, Long postId) {
        System.out.println("🔸 createComment() called for userId=" + author.getUserId());

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        System.out.println("🔸 post found: id=" + post.getId() + ", festivalSlug=" + post.getFestival().getSlug());



        System.out.println("✅ authorId=" + author.getUserId());
        System.out.println("✅ postSlug=" + post.getFestival().getSlug());
        System.out.println("✅ userFestivalRepository test=" + userFestivalRepository.count());


        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(author.getUserId(), post.getFestival().getSlug())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));



        System.out.println("userFestival id = " + userFestival.getId());
        System.out.println("userFestival user = " + userFestival.getUser());
        System.out.println("userFestival user id = " + userFestival.getUser().getUserId());


        Comment comment = Comment.builder()
                .author(userFestival)
                .post(post)
                .content(request.getContent())
                .build();



        return commentRepository.save(comment);
    }

    // 댓글 수정하기
    @Transactional
    public Comment updateComment( User author, CommentRequest request,Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자만 수정 권한을 가짐
        if (!comment.getAuthor().getUser().getUserId().equals(author.getUserId())) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }
        comment.updateContent(request.getContent());
        return commentRepository.save(comment);

    }

    // 댓글 삭제하기
    @Transactional
    public void deleteComment( User author, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getAuthor().getUser().getUserId().equals(author.getUserId())) {
            throw new SecurityException("본인만 댓글을 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
        comment.getPost().decreaseCommentCount();
    }


    // 댓글 조회
    @Transactional
    public List<CommentResponse> getCommentsByPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);
        return comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }
}



