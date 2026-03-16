package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.entity.SiteSetting;
import com.lookforbest.repository.SiteSettingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "站点设置", description = "站点全局配置管理")
public class SiteSettingController {

    private final SiteSettingRepository settingRepository;

    /** 公开接口：获取所有站点设置（前台页面使用） */
    @GetMapping
    @Operation(summary = "获取站点设置")
    public ApiResponse<Map<String, String>> getAll() {
        Map<String, String> map = new LinkedHashMap<>();
        settingRepository.findAll().forEach(s -> map.put(s.getSettingKey(), s.getSettingValue()));
        return ApiResponse.ok(map);
    }

    /** 管理员：批量更新设置 */
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "更新站点设置（管理员）")
    public ApiResponse<?> update(@RequestBody Map<String, String> settings) {
        for (var entry : settings.entrySet()) {
            SiteSetting s = settingRepository.findById(entry.getKey()).orElseGet(() -> {
                SiteSetting ns = new SiteSetting();
                ns.setSettingKey(entry.getKey());
                return ns;
            });
            s.setSettingValue(entry.getValue());
            s.setUpdatedAt(LocalDateTime.now());
            settingRepository.save(s);
        }
        return ApiResponse.ok("更新成功");
    }
}
