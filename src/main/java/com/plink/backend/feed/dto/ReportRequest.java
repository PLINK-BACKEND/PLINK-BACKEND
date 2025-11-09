package com.plink.backend.feed.dto;

import com.plink.backend.feed.entity.ReportReason;
import com.plink.backend.feed.entity.ReportTargetType;
import lombok.Data;
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
