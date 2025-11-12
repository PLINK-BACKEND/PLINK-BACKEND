package com.plink.backend.feed.repository.report;

import com.plink.backend.feed.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
