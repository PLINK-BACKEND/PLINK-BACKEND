package com.plink.backend.feed.service.comment;

import com.plink.backend.feed.dto.comment.CommentRequest;
import com.plink.backend.feed.dto.comment.CommentResponse;
import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.post.Post;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.feed.repository.comment.CommentRepository;
import com.plink.backend.feed.repository.post.PostRepository;
import com.plink.backend.user.repository.UserFestivalRepository;
import com.plink.backend.user.entity.UserFestival;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserFestivalRepository userFestivalRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    // 댓글 작성하기
    @Transactional
    public CommentResponse createComment(User author, CommentRequest request, String slug, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(author.getUserId(), post.getFestival().getSlug())
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .author(userFestival)
                .post(post)
                .content(request.getContent())
                .build();

        post.increaseCommentCount();
        commentRepository.save(comment);

        CommentResponse response = CommentResponse.from(comment);

        // DB 커밋 완료 후 메시지 전송 (게시글 방식 동일)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                String topicPath = String.format("/topic/%s/posts/%d/comments", slug, postId);
                log.info("Broadcasting Comment to {}", topicPath);
                messagingTemplate.convertAndSend(topicPath, response);
            }
        });

        return response;
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

        Post post = comment.getPost();
        Long postId = post.getId();
        String slug = post.getFestival().getSlug();
        Long deletedCommentId = comment.getId();

        commentRepository.delete(comment);
        comment.getPost().decreaseCommentCount();

        // DB 커밋 완료 후 메시지 전송 (게시글 방식 동일)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                Map<String, Object> payload = new HashMap<>();
                payload.put("type", "DELETE");        // 이벤트 타입
                payload.put("id", deletedCommentId);  // 삭제된 댓글 ID
                payload.put("deleted", true);         // 삭제 여부 플래그

                String topicPath = String.format("/topic/%s/posts/%d/comments", slug, postId);

                log.info(" 댓글 삭제 전송: {}", topicPath);
                messagingTemplate.convertAndSend(topicPath, payload);
            }
        });

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



