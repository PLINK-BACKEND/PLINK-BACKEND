package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.*;

import com.plink.backend.user.entity.User;
import com.plink.backend.main.entity.Festival;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    @Column(nullable = false)
    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private int commentCount = 0;
    private int likeCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();



    @Builder
    public Post(PostType postType, String title, String content,
                User author, Tag tag, Festival festival) {
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.author = author;
        this.tag = tag;
        this.festival = festival;
        this.comments = new ArrayList<>();
        this.images = new ArrayList<>();
        this.likes = new ArrayList<>();
    }

    // ====== 비즈니스 로직 ======

    // 제목 수정
    public void updateTitle(String title) {
        this.title = title;
    }

    // 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }

    // 태그 수정
    public void updateTag(Tag tag) {
        this.tag = tag;
    }

    // 이미지 추가
    public void addImage(Image image) {
        images.add(image);
        image.setPost(this);
    }

    // 댓글 수 갱신
    public void updateCommentCount(int count) {
        this.commentCount = count;
    }

    // 좋아요 수 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
