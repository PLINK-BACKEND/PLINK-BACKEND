package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.HiddenContent;
import com.plink.backend.feed.entity.ReportTargetType;
import com.plink.backend.user.entity.UserFestival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HiddenContentRepository extends JpaRepository<HiddenContent, Long> {
    @Query("SELECT h.targetId FROM HiddenContent h " +
            "WHERE h.user = :user AND h.targetType = :targetType")
    List<Long> findTargetIdsByUserFestivalAndTargetType(@Param("user") UserFestival user,
                                                @Param("targetType") ReportTargetType targetType);
}
