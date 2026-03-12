package com.lookforbest.repository;

import com.lookforbest.entity.RobotImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RobotImageRepository extends JpaRepository<RobotImage, Long> {

    List<RobotImage> findByRobotIdOrderBySortOrderAsc(Long robotId);
}
