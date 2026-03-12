package com.lookforbest.repository;

import com.lookforbest.entity.RobotModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RobotModelRepository extends JpaRepository<RobotModel, Long> {

    Optional<RobotModel> findByRobotId(Long robotId);
}
