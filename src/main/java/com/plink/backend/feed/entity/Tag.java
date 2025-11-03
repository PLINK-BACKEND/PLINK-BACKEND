package com.plink.backend.feed.entity;

import jakarta.persistence.*;

import java.util.ArrayList;

@Entity

public class Tag {
    @Id @GeneratedValue
    private  Long id;

    @Column(unique=true)
    private String tag_name;


}
