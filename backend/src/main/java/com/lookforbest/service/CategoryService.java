package com.lookforbest.service;

import com.lookforbest.entity.RobotCategory;

import java.util.List;

public interface CategoryService {
    List<RobotCategory> findAll();
    RobotCategory findById(Long id);
}
