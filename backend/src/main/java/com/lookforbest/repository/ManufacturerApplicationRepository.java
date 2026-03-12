package com.lookforbest.repository;

import com.lookforbest.entity.ManufacturerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManufacturerApplicationRepository extends JpaRepository<ManufacturerApplication, Long> {

    List<ManufacturerApplication> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<ManufacturerApplication> findByStatus(ManufacturerApplication.Status status, Pageable pageable);

    Optional<ManufacturerApplication> findByUserIdAndStatus(Long userId, ManufacturerApplication.Status status);
}
