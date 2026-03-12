package com.lookforbest.repository;

import com.lookforbest.entity.RobotCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RobotCategoryRepository extends JpaRepository<RobotCategory, Long> {

    List<RobotCategory> findByParentIdIsNullOrderBySortOrderAsc();

    List<RobotCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

    Optional<RobotCategory> findBySlug(String slug);
}
