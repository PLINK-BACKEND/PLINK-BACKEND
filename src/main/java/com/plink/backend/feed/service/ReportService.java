package com.plink.backend.feed.service;

import com.plink.backend.feed.dto.ReportRequest;
import com.plink.backend.feed.entity.HiddenContent;
import com.plink.backend.feed.entity.Report;
import com.plink.backend.feed.repository.HiddenContentRepository;
import com.plink.backend.feed.repository.ReportRepository;
import com.plink.backend.global.exception.CustomException;
import com.plink.backend.user.entity.User;
import com.plink.backend.user.entity.UserFestival;
import com.plink.backend.user.repository.UserFestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final HiddenContentRepository hiddenContentRepository;
    private final UserFestivalRepository userFestivalRepository;

    @Transactional
    public void createReport(User reporter, ReportRequest request,String slug) {

        // 작성자-축제 매핑 검증
        UserFestival userFestival = userFestivalRepository
                .findByUser_UserIdAndFestivalSlug(reporter.getUserId(), slug)
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "해당 축제에서 유저를 찾을 수 없습니다."));

        // 신고 저장
        Report report = Report.builder()
                .reporter(userFestival)
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .details(request.getDetails())
                .build();

        reportRepository.save(report);

        // 신고한 사용자에게는 해당 콘텐츠 숨김 처리
        hiddenContentRepository.save(
                HiddenContent.builder()
                        .user(userFestival)
                        .targetType(request.getTargetType())
                        .targetId(request.getTargetId())
                        .build()
        );
    }
}
