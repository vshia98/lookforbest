package com.lookforbest.controller;

import com.lookforbest.dto.request.ManufacturerApplicationRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.ManufacturerPortalDTO;
import com.lookforbest.entity.ManufacturerApplication;
import com.lookforbest.entity.User;
import com.lookforbest.repository.UserRepository;
import com.lookforbest.service.ManufacturerPortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/manufacturer-portal")
@RequiredArgsConstructor
public class ManufacturerPortalController {

    private final ManufacturerPortalService portalService;
    private final UserRepository userRepository;

    /** 查询本人申请状态 */
    @GetMapping("/my-application")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManufacturerApplication> getMyApplication(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok(portalService.getMyApplication(userId).orElse(null));
    }

    /** 提交入驻申请 */
    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManufacturerApplication> apply(
            @Valid @RequestBody ManufacturerApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        return ApiResponse.ok(portalService.applyForPortal(userId, request));
    }

    /** 获取厂商仪表盘数据（需 manufacturer 或 admin 角色） */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ManufacturerPortalDTO> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        ManufacturerPortalDTO dto = portalService.getMyManufacturer(userId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "您尚未拥有厂商门户"));
        return ApiResponse.ok(dto);
    }

    /** 更新厂商资料（需 manufacturer 角色） */
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('MANUFACTURER', 'ADMIN', 'SUPERADMIN')")
    public ApiResponse<Void> updateProfile(
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = requireUserId(userDetails);
        portalService.updateManufacturerProfile(userId, updates);
        return ApiResponse.ok();
    }

    private Long requireUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(User::getId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "用户不存在"));
    }
}
