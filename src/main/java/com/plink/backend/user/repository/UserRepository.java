package com.plink.backend.user.repository;

import com.plink.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // slug + nickname 조합으로 유저 조회 (게스트→회원 전환 시 사용)
    Optional<User> findByNicknameAndSlug(String nickname, String slug);

    // slug + nickname 중복 여부 확인 (게스트/회원 통합 중복검사)
    boolean existsBySlugAndNickname(String slug, String nickname);
}
