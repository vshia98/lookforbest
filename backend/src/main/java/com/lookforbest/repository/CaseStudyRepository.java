package com.lookforbest.repository;

import com.lookforbest.entity.CaseStudy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaseStudyRepository extends JpaRepository<CaseStudy, Long> {

    Page<CaseStudy> findByStatus(CaseStudy.Status status, Pageable pageable);

    Page<CaseStudy> findByIndustryAndStatus(String industry, CaseStudy.Status status, Pageable pageable);

    Page<CaseStudy> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<CaseStudy> findByIdAndUserId(Long id, Long userId);
}
