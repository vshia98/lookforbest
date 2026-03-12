package com.lookforbest.repository;

import com.lookforbest.entity.RobotReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RobotReviewRepository extends JpaRepository<RobotReview, Long> {

    Page<RobotReview> findByRobotIdAndStatus(Long robotId, RobotReview.Status status, Pageable pageable);

    Page<RobotReview> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByRobotIdAndStatus(Long robotId, RobotReview.Status status);

    Optional<RobotReview> findByIdAndUserId(Long id, Long userId);

    Page<RobotReview> findByStatus(RobotReview.Status status, Pageable pageable);
}
