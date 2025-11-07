package com.plink.backend.user.repository;

import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFestivalRepository extends JpaRepository<UserFestival, Long> {
    // 행사(slug) + 닉네임 중복 체크
    boolean existsByFestivalSlugAndNickname(String festivalSlug, String nickname);
}
