package com.lookforbest.controller;

import com.lookforbest.dto.response.ApiResponse;
import com.lookforbest.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "文件上传", description = "图片/视频/3D模型文件上传接口")
public class FileController {

    private final FileUploadService fileUploadService;

    /**
     * 上传文件到 MinIO，返回公开访问 URL。
     * 支持：jpg/png/webp/gif（图片）、mp4/mov/avi（视频）、glb/gltf（3D模型）
     * 文件大小限制：50MB
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "上传文件", description = "支持图片(jpg/png/webp/gif)、视频(mp4)、3D模型(glb/gltf)，最大50MB")
    public ApiResponse<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.upload(file);
            return ApiResponse.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 管理员上传（无需额外权限校验，已通过 ROLE_ADMIN 限制整个 /admin/ 前缀）
     * 此端点用于管理后台的文件上传，路径保持和前端一致
     */
    @PostMapping(value = "/upload/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "管理员上传文件")
    public ApiResponse<Map<String, String>> adminUpload(@RequestParam("file") MultipartFile file) {
        try {
            String url = fileUploadService.upload(file);
            return ApiResponse.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}
