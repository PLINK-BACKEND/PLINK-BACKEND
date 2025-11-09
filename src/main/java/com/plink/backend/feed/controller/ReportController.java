package com.plink.backend.feed.controller;

import com.plink.backend.feed.dto.ReportRequest;
import com.plink.backend.feed.service.ReportService;
import com.plink.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{slug}/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<String> createReport(
            @PathVariable String slug,
            @AuthenticationPrincipal User reporter,
            @ModelAttribute ReportRequest request) {

        reportService.createReport(reporter, request,slug);
        return ResponseEntity.ok("신고가 접수되었습니다.");
    }
}
