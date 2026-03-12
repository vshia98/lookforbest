package com.lookforbest.repository;

import com.lookforbest.entity.UgcReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UgcReportRepository extends JpaRepository<UgcReport, Long> {

    Page<UgcReport> findByStatus(UgcReport.ReportStatus status, Pageable pageable);
}
