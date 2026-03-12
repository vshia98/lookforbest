package com.lookforbest.service.impl;

import com.lookforbest.entity.RobotCategory;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.RobotCategoryRepository;
import com.lookforbest.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final RobotCategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RobotCategory> findAll() {
        return categoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public RobotCategory findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
