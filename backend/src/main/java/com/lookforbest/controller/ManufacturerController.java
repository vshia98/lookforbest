package com.lookforbest.controller;

import com.lookforbest.dto.request.ManufacturerCreateRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.ManufacturerDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.entity.Manufacturer;
import com.lookforbest.service.ManufacturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manufacturers")
@RequiredArgsConstructor
@Tag(name = "厂商", description = "机器人厂商相关接口")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @GetMapping
    @Operation(summary = "获取厂商列表")
    public ApiResponse<?> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = manufacturerService.findAll(q, country, PageRequest.of(page, size));
        return ApiResponse.ok(PagedResponse.of(result.map(ManufacturerDTO::from)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取厂商详情")
    public ApiResponse<?> getById(@PathVariable Long id) {
        return ApiResponse.ok(ManufacturerDTO.from(manufacturerService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "创建厂商（管理员）")
    public ApiResponse<?> create(@Valid @RequestBody ManufacturerCreateRequest request) {
        Manufacturer m = new Manufacturer();
        m.setName(request.getName());
        m.setNameEn(request.getNameEn());
        m.setCountry(request.getCountry());
        m.setCountryCode(request.getCountryCode());
        m.setLogoUrl(request.getLogoUrl());
        m.setWebsiteUrl(request.getWebsiteUrl());
        m.setFoundedYear(request.getFoundedYear());
        m.setDescription(request.getDescription());
        m.setDescriptionEn(request.getDescriptionEn());
        m.setHeadquarters(request.getHeadquarters());
        return ApiResponse.ok(ManufacturerDTO.from(manufacturerService.create(m)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "更新厂商（管理员）")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody ManufacturerCreateRequest request) {
        Manufacturer m = new Manufacturer();
        m.setName(request.getName());
        m.setNameEn(request.getNameEn());
        m.setCountry(request.getCountry());
        m.setCountryCode(request.getCountryCode());
        m.setLogoUrl(request.getLogoUrl());
        m.setWebsiteUrl(request.getWebsiteUrl());
        m.setFoundedYear(request.getFoundedYear());
        m.setDescription(request.getDescription());
        m.setDescriptionEn(request.getDescriptionEn());
        m.setHeadquarters(request.getHeadquarters());
        return ApiResponse.ok(ManufacturerDTO.from(manufacturerService.update(id, m)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    @Operation(summary = "删除厂商（管理员）")
    public ApiResponse<?> delete(@PathVariable Long id) {
        manufacturerService.delete(id);
        return ApiResponse.ok();
    }
}
