package com.lookforbest.repository;

import com.lookforbest.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
    Optional<PriceAlert> findByUserIdAndRobotId(Long userId, Long robotId);
    List<PriceAlert> findByRobotIdAndActiveTrueAndTriggeredFalse(Long robotId);
    List<PriceAlert> findByUserId(Long userId);
}
