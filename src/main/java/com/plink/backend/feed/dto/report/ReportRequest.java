package com.plink.backend.feed.dto.report;

import com.plink.backend.feed.entity.report.ReportReason;
import com.plink.backend.feed.entity.report.ReportTargetType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {
    private ReportTargetType targetType; // POST or COMMENT
    private Long targetId;
    private ReportReason reason;
    private String details;
}
