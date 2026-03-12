package com.lookforbest.repository;

import com.lookforbest.entity.AdClickLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AdClickLogRepository extends JpaRepository<AdClickLog, Long> {

    /**
     * 统计指定广告在时间段内的点击次数
     */
    long countByAdIdAndClickedAtBetween(Long adId, LocalDateTime from, LocalDateTime to);
}
