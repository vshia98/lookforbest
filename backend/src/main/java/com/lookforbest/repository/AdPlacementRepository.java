package com.lookforbest.repository;

import com.lookforbest.entity.AdPlacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface AdPlacementRepository extends JpaRepository<AdPlacement, Long> {

    /**
     * 查询指定位置的有效广告（含日期范围判断），按优先级降序
     */
    @Query("SELECT a FROM AdPlacement a WHERE a.position = :position AND a.isActive = true " +
           "AND (a.startDate IS NULL OR a.startDate <= :today) " +
           "AND (a.endDate IS NULL OR a.endDate >= :today) " +
           "ORDER BY a.priority DESC")
    List<AdPlacement> findActiveAdsByPosition(
            @Param("position") AdPlacement.Position position,
            @Param("today") LocalDate today);

    /**
     * 按创建时间倒序查全部广告
     */
    List<AdPlacement> findAllByOrderByCreatedAtDesc();

    /**
     * 增加展示次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE AdPlacement a SET a.impressionCount = a.impressionCount + 1 WHERE a.id = :id")
    void incrementImpressionCount(@Param("id") Long id);

    /**
     * 增加点击次数
     */
    @Modifying
    @Transactional
    @Query("UPDATE AdPlacement a SET a.clickCount = a.clickCount + 1 WHERE a.id = :id")
    void incrementClickCount(@Param("id") Long id);

    /**
     * CTR 报告：返回 adId, title, impressions, clicks, ctr
     */
    @Query("SELECT a.id AS adId, a.title AS title, a.impressionCount AS impressions, " +
           "a.clickCount AS clicks, " +
           "CASE WHEN a.impressionCount = 0 THEN 0.0 " +
           "ELSE (CAST(a.clickCount AS double) / CAST(a.impressionCount AS double)) * 100 END AS ctr " +
           "FROM AdPlacement a ORDER BY a.createdAt DESC")
    List<Map<String, Object>> findCtrStats();
}
