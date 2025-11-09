package com.plink.backend.feed.repository;

import com.plink.backend.feed.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
