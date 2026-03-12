package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.dto.response.PagedResponse;
import com.lookforbest.dto.response.RobotSummaryDTO;
import com.lookforbest.dto.response.UserDTO;
import com.lookforbest.service.FavoriteService;
import com.lookforbest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户", description = "当前用户信息及收藏")
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public ApiResponse<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.ok(UserDTO.from(userService.getCurrentUser(userDetails.getUsername())));
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "获取收藏列表")
    public ApiResponse<?> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var robots = favoriteService.getFavorites(userDetails.getUsername(), PageRequest.of(page, size));
        return ApiResponse.ok(PagedResponse.of(robots.map(RobotSummaryDTO::from)));
    }

    @PostMapping("/me/favorites/{robotId}")
    @Operation(summary = "添加收藏")
    public ApiResponse<?> addFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long robotId) {
        favoriteService.addFavorite(userDetails.getUsername(), robotId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/me/favorites/{robotId}")
    @Operation(summary = "取消收藏")
    public ApiResponse<?> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long robotId) {
        favoriteService.removeFavorite(userDetails.getUsername(), robotId);
        return ApiResponse.ok();
    }
}
