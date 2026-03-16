package com.lookforbest.controller;

import com.lookforbest.dto.request.InquiryCreateRequest;
import com.lookforbest.dto.request.InquiryReplyRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.InquiryDTO;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
@Tag(name = "询价", description = "询价/联系厂商接口")
public class InquiryController {

    private final InquiryService inquiryService;

    /** 提交询价（需登录） */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "提交询价")
    public ApiResponse<InquiryDTO> create(
            @Valid @RequestBody InquiryCreateRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.ok(inquiryService.createInquiry(user.getUsername(), req));
    }

    /** 获取当前用户询价列表 */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取我的询价列表")
    public ApiResponse<PagedResponse<InquiryDTO>> myInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ApiResponse.ok(inquiryService.getMyInquiries(user.getUsername(), page, size));
    }

    /** 获取单条询价详情（询价人或管理员） */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取询价详情")
    public ApiResponse<InquiryDTO> get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));
        return ApiResponse.ok(inquiryService.getInquiry(id, user.getUsername(), isAdmin));
    }

    /** 管理员：获取所有询价列表 */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "管理员获取所有询价")
    public ApiResponse<PagedResponse<InquiryDTO>> adminList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(inquiryService.adminListAll(page, size));
    }

    /** 管理员：回复询价 */
    @PostMapping("/admin/{id}/reply")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "管理员回复询价")
    public ApiResponse<InquiryDTO> adminReply(
            @PathVariable Long id,
            @Valid @RequestBody InquiryReplyRequest req) {
        return ApiResponse.ok(inquiryService.adminReply(id, req));
    }

    /** 管理员：更新询价状态 */
    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @Operation(summary = "管理员更新询价状态")
    public ApiResponse<InquiryDTO> adminUpdateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ApiResponse.ok(inquiryService.adminUpdateStatus(id, status));
    }
}
