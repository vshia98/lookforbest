package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.CategoryDTO;
import com.lookforbest.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "分类", description = "机器人分类接口")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "获取分类树")
    public ApiResponse<?> list() {
        List<CategoryDTO> categories = categoryService.findAll()
                .stream()
                .map(CategoryDTO::from)
                .collect(Collectors.toList());
        return ApiResponse.ok(categories);
    }
}
