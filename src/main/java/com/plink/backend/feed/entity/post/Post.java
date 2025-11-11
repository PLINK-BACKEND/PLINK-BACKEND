package com.plink.backend.feed.entity.post;

import com.plink.backend.feed.entity.tag.Tag;
import com.plink.backend.feed.entity.comment.Comment;
import com.plink.backend.feed.entity.poll.Poll;
import com.plink.backend.user.entity.UserFestival;
import jakarta.persistence.*;
import lombok.*;

import com.plink.backend.main.entity.Festival;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    @JoinColumn(name = "user_festival_id", nullable = false)
    private UserFestival author;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Image> images = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    Poll poll;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "comment_count", nullable = false)
    private int commentCount =0;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();



    @Builder
    public Post(PostType postType, String title, String content,
                UserFestival author, Tag tag, Festival festival) {
        this.postType = postType;
        this.title = title;
        this.content = content;
        this.author = author;
        this.tag = tag;
        this.festival = festival;
        this.comments = new HashSet<>();
        this.images = new  HashSet<>();
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

    // 앙케이트
    public void setPoll(Poll poll) {
        this.poll = poll;
        if(poll.getPost() != this){
            poll.setPost(this);
        }
    }

    // 댓글 수 증가
    public void increaseCommentCount() {
            this.commentCount = this.commentCount + 1;

    }

    // 댓글 수 감소
    public void  decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
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
