package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> { }