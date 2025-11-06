package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.catalina.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자
@AllArgsConstructor
@Builder
public class Comment {

    @Id @GeneratedValue
    private  Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User author; //추후 로그인 회원가입 완료되면 수정

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreationTimestamp
    private LocalDateTime updatedAt;

    private int likeCount = 0;

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> likes = new ArrayList<>();

    public void updateContent(String content){ this.content = content; }

    // 좋아요 수 증가
    public void increaseLikeCount() { this.likeCount++;}

    // 좋아요 수 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
