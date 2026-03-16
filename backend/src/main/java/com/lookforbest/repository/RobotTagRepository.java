package com.lookforbest.repository;

import com.lookforbest.entity.RobotTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RobotTagRepository extends JpaRepository<RobotTag, Long> {

    Optional<RobotTag> findByName(String name);

    Optional<RobotTag> findBySlug(String slug);

    List<RobotTag> findByNameIn(List<String> names);

    /** 热门标签（按引用计数降序） */
    List<RobotTag> findTop30ByOrderByUsageCountDesc();

    /** 刷新引用计数 */
    @Modifying
    @Query("UPDATE RobotTag t SET t.usageCount = (SELECT COUNT(r) FROM Robot r JOIN r.tags rt WHERE rt.id = t.id)")
    void refreshAllUsageCounts();
}
