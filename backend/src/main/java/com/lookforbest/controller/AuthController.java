package com.lookforbest.controller;

import com.lookforbest.dto.request.LoginRequest;
import com.lookforbest.dto.request.OAuthLoginRequest;
import com.lookforbest.dto.request.RefreshTokenRequest;
import com.lookforbest.dto.request.RegisterRequest;
import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.service.OAuthService;
import com.lookforbest.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证", description = "用户注册/登录/token刷新/社交登录")
public class AuthController {

    private final UserService userService;
    private final OAuthService oAuthService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<?> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token")
    public ApiResponse<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(userService.refreshToken(request));
    }

    @PostMapping("/oauth/login")
    @Operation(summary = "社交账号登录（Google / 微信）")
    public ApiResponse<?> oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        return ApiResponse.ok(oAuthService.login(request));
    }
}
