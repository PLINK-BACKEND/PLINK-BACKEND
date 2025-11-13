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

    // 특정 유저가 특정 축제(slug)에 속해있는 UserFestival 정보 조회 (특정 축제에 이미 참여했는지)
    Optional<UserFestival> findByUser_UserIdAndFestivalSlug(Long userId, String festivalSlug);

    // slug + nickname 조합으로 유저 조회 (게스트→회원 전환 시 사용)
    Optional<User> findByNicknameAndFestivalSlug(String nickname, String slug);

    boolean existsByNicknameAndFestivalSlug(String nickname, String festivalSlug);

}
