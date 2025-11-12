package com.plink.backend.festival.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity

public class Festival {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    private LocalDate startDate;
    private LocalDate endDate;

    private String location;

    // slug 자동 생성
    public void generateSlug(){
        this.slug = name
                .toLowerCase()
                .replaceAll("[^a-z0-9가-힣 ]", "")
                .replace(" ", "-");
    }

}
