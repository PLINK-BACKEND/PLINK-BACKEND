package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import org.apache.catalina.User;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id @GeneratedValue
    private  Long id;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user; //추후 로그인 회원가입 완료되면 수정

    private String body;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @CreationTimestamp
    private LocalDateTime updatedAt;
}
