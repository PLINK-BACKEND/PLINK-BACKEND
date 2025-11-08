package com.plink.backend.feed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity

public class Tag {
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(unique=true)
    private String tag_name;


}
