package com.plink.backend.feed.repository.post;

import com.plink.backend.feed.entity.post.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

}