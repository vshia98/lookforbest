package com.lookforbest.controller;

import com.lookforbest.dto.request.TrackActionRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * 公开接口：前端上报用户行为
     * POST /api/v1/analytics/track
     */
    @PostMapping("/api/v1/analytics/track")
    public ApiResponse<Void> track(
            @Valid @RequestBody TrackActionRequest req,
            Authentication auth,
            HttpServletRequest httpRequest) {
        Long userId = null;
        if (auth != null && auth.isAuthenticated()) {
            try {
                userId = Long.valueOf(auth.getName());
            } catch (NumberFormatException ignored) { }
        }
        analyticsService.track(req, userId, httpRequest);
        return ApiResponse.ok();
    }

    /**
     * 管理员：总览统计
     * GET /api/v1/admin/analytics/overview
     */
    @GetMapping("/api/v1/admin/analytics/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(analyticsService.getOverview());
    }

    /**
     * 管理员：每日趋势
     * GET /api/v1/admin/analytics/daily-trends?days=30
     */
    @GetMapping("/api/v1/admin/analytics/daily-trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<List<Map<String, Object>>> dailyTrends(
            @RequestParam(defaultValue = "30") int days) {
        if (days < 1 || days > 365) days = 30;
        return ApiResponse.ok(analyticsService.getDailyTrends(days));
    }

    /**
     * 管理员：今日每小时分布
     * GET /api/v1/admin/analytics/hourly-today
     */
    @GetMapping("/api/v1/admin/analytics/hourly-today")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<List<Map<String, Object>>> hourlyToday() {
        return ApiResponse.ok(analyticsService.getHourlyToday());
    }

    /**
     * 管理员：热门机器人
     * GET /api/v1/admin/analytics/top-robots?days=7&limit=10
     */
    @GetMapping("/api/v1/admin/analytics/top-robots")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<List<Map<String, Object>>> topRobots(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        if (days < 1 || days > 365) days = 7;
        if (limit < 1 || limit > 50) limit = 10;
        return ApiResponse.ok(analyticsService.getTopRobots(days, limit));
    }

    /**
     * 管理员：热门搜索词
     * GET /api/v1/admin/analytics/top-keywords?days=7&limit=20
     */
    @GetMapping("/api/v1/admin/analytics/top-keywords")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ApiResponse<List<Map<String, Object>>> topKeywords(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "20") int limit) {
        if (days < 1 || days > 365) days = 7;
        if (limit < 1 || limit > 100) limit = 20;
        return ApiResponse.ok(analyticsService.getTopSearchKeywords(days, limit));
    }
}
