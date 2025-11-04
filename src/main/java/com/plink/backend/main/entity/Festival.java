package com.plink.backend.main.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity

public class Festival {
    @Id @GeneratedValue
    private  Long id;

    private String name;
    private String slug;

    private LocalDate startDate;
    private LocalDate endDate;

    private Boolean is_active;

    @CreationTimestamp
    private  LocalDateTime createdAt;
    @CreationTimestamp
    private  LocalDateTime updatedAt;



}
