package com.lookforbest.controller;

import com.lookforbest.dto.request.AnnouncementRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.NotificationDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知", description = "站内通知接口")
public class NotificationController {

    private final NotificationService notificationService;

    /** 获取当前用户通知列表 */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的通知列表")
    public ApiResponse<PagedResponse<NotificationDTO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.ok(notificationService.getMyNotifications(user.getUsername(), page, size));
    }

    /** 未读数量 */
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取未读通知数量")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserDetails user) {
        long count = notificationService.getUnreadCount(user.getUsername());
        return ApiResponse.ok(Map.of("count", count));
    }

    /** 标记单条已读 */
    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "标记通知已读")
    public ApiResponse<Void> markRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        notificationService.markRead(id, user.getUsername());
        return ApiResponse.ok();
    }

    /** 全部标记已读 */
    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "全部标记已读")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserDetails user) {
        notificationService.markAllRead(user.getUsername());
        return ApiResponse.ok();
    }

    /** 管理员：发布系统公告 */
    @PostMapping("/admin/announcement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "管理员发布系统公告（广播全部用户）")
    public ApiResponse<Void> announcement(@Valid @RequestBody AnnouncementRequest req) {
        notificationService.broadcastAnnouncement(req);
        return ApiResponse.ok();
    }
}
