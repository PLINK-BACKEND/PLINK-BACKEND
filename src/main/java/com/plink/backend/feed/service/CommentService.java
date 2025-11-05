package com.plink.backend.feed.service;

import com.plink.backend.feed.dto.CommentRequest;
import com.plink.backend.feed.dto.CommentResponse;
import com.plink.backend.feed.entity.Comment;
import com.plink.backend.feed.entity.Post;
import com.plink.backend.feed.repository.CommentRepository;
import com.plink.backend.feed.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 작성하기
    @Transactional
    public Comment createComment(User author, CommentRequest request,Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        Comment comment = Comment.builder()
                .author(author)
                .post(post)
                .content(request.getContent())
                .build();

        return commentRepository.save(comment);
    }

    // 댓글 수정하기
    @Transactional
    public Comment updateComment( User Author, CommentRequest request,Long commentId,) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(()-> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자만 수정 권한을 가짐
        if (!comment.getAuthor().equals(Author)){
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다."));
        }

        comment.updateContent(request.getContent());
        return commentRepository.save(comment);



    }

    // 댓글 삭제하기
    @Transactional
    public void deleteComment( User author, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        if (!comment.getAuthor().equals(author)) {
            throw new SecurityException("본인만 댓글을 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
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



