package com.lookforbest.service;

import com.lookforbest.entity.Robot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RobotService {

    Page<Robot> findAll(
            String q, Long categoryId, Long manufacturerId, List<Long> domainIds,
            BigDecimal payloadMin, BigDecimal payloadMax,
            Integer reachMin, Integer reachMax,
            Integer dofMin, Integer dofMax,
            Boolean has3dModel, String status, String sort, Pageable pageable
    );

    Robot findById(Long id);

    Robot findBySlug(String slug);

    List<Robot> findSimilar(Long id, int size);

    Robot create(Robot robot);

    Robot update(Long id, Robot robot);

    void delete(Long id);
}
