package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.*;

import com.plink.backend.user.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import com.plink.backend.main.entity.Festival;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    @Column(nullable = false)
    private String title;
    private String content;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private int commentCount = 0;
    private int likeCount = 0;

    @OneToMany(mappedBy = "post",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    public void updateTitle(String title) {this.title = title; }
    public void updateContent(String content) {
        this.content = content;
    }
    public void updateTag(Tag tag) { this.tag = tag; }

    // 댓글 수 반환
    public void updateCommentCount(int count)  { this.commentCount = count; }

    // 좋아요 수 증가
    public void increaseLikeCount() { this.likeCount++;}

    // 좋아요 수 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
