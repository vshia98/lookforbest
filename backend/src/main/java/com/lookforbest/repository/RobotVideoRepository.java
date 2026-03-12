package com.lookforbest.repository;

import com.lookforbest.entity.RobotVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RobotVideoRepository extends JpaRepository<RobotVideo, Long> {

    List<RobotVideo> findByRobotIdOrderBySortOrderAsc(Long robotId);
}
