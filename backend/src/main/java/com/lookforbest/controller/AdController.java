package com.lookforbest.controller;

import com.lookforbest.dto.response.AdPlacementDTO;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.service.AdService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 广告公开接口
 */
@RestController
@RequestMapping("/api/v1/ads")
@RequiredArgsConstructor
public class AdController {

    private final AdService adService;

    /**
     * 获取指定位置的有效广告列表
     * GET /api/v1/ads?position=home_banner
     */
    @GetMapping
    public ApiResponse<List<AdPlacementDTO>> getActiveAds(@RequestParam String position) {
        return ApiResponse.ok(adService.getActiveAds(position));
    }

    /**
     * 记录广告点击
     * POST /api/v1/ads/{id}/click
     */
    @PostMapping("/{id}/click")
    public ApiResponse<Void> recordClick(@PathVariable Long id, HttpServletRequest request) {
        String ip = extractClientIp(request);
        adService.recordClick(id, ip, null);
        return ApiResponse.ok();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
