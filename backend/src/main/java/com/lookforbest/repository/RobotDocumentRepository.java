package com.lookforbest.repository;

import com.lookforbest.entity.RobotDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RobotDocumentRepository extends JpaRepository<RobotDocument, Long> {

    List<RobotDocument> findByRobotIdOrderByCreatedAtAsc(Long robotId);
}
