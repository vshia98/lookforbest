package com.lookforbest.repository;

import com.lookforbest.entity.RobotPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RobotPriceHistoryRepository extends JpaRepository<RobotPriceHistory, Long> {
    List<RobotPriceHistory> findByRobotIdOrderByRecordedAtAsc(Long robotId);
    List<RobotPriceHistory> findByRobotIdAndRecordedAtAfterOrderByRecordedAtAsc(Long robotId, LocalDateTime after);
}
