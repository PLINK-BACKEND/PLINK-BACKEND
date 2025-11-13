package com.plink.backend.feed.service.report;

import com.plink.backend.feed.dto.report.ReportRequest;
import com.plink.backend.feed.entity.report.HiddenContent;
import com.plink.backend.feed.entity.report.Report;
import com.plink.backend.feed.repository.report.HiddenContentRepository;
import com.plink.backend.feed.repository.report.ReportRepository;
import com.plink.backend.feed.service.post.PostService;
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
    private final PostService postService;

    @Transactional
    public void createReport(User reporter, ReportRequest request,String slug) {

        // 작성자-축제 매핑 검증
        UserFestival userFestival = postService.getVerifiedUserFestival(reporter, slug);

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
