package com.lookforbest.controller;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * 面向公众的静态文件代理：
 * 将 /lookforbest/{prefix}/{filename} 请求通过后端转发到 MinIO，
 * 用于机器人封面图、厂商 logo 等资源的统一访问入口。
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class PublicFileController {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket}")
    private String bucket;

    @GetMapping("/lookforbest/{prefix}/{filename:.+}")
    public ResponseEntity<InputStreamResource> getObject(
            @PathVariable String prefix,
            @PathVariable String filename
    ) {
        String objectName = prefix + "/" + filename;
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );

            MediaType mediaType = resolveMediaType(filename);
            InputStreamResource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            // 允许前端长时间缓存图片资源
            headers.setCacheControl("public, max-age=31536000, immutable");

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (ErrorResponseException e) {
            // 对象不存在或无权限时返回 404，避免直接把 MinIO 错误暴露给前端
            if ("NoSuchKey".equals(e.errorResponse().code())
                    || "NoSuchBucket".equals(e.errorResponse().code())
                    || "AccessDenied".equals(e.errorResponse().code())) {
                log.warn("MinIO 对象不存在或不可访问: bucket={}, object={}", bucket, objectName);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            log.error("从 MinIO 读取对象失败: bucket={}, object={}, code={}",
                    bucket, objectName, e.errorResponse().code(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (MinioException e) {
            log.error("MinIO 访问异常: bucket={}, object={}", bucket, objectName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("读取文件失败: bucket={}, object={}", bucket, objectName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".webp")) {
            return MediaType.valueOf("image/webp");
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        }
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        if (lower.endsWith(".svg")) {
            return MediaType.valueOf("image/svg+xml");
        }
        // 其它未知类型按二进制流返回
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}

