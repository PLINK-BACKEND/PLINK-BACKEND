package com.plink.backend.user.repository;

import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFestivalRepository extends JpaRepository<UserFestival, Long> {
    // 행사(slug) + 닉네임 중복 체크
    boolean existsByFestivalSlugAndNickname(String festivalSlug, String nickname);
    // 유저의 행사리스트 조회
    List<UserFestival> findByUser_UserId(Long userId);

    Optional<UserFestival> findByUserAndFestivalSlug(User user, String festivalSlug);

    // 특정 유저가 특정 축제(slug)에 속해있는 UserFestival 정보 조회
    Optional<UserFestival> findByUser_UserIdAndFestivalSlug(Long userId, String festivalSlug);

    // 유저가 이미 특정 축제에 참여했는지 확인
    boolean existsByUserAndFestivalSlug(User user, String festivalSlug);
}
