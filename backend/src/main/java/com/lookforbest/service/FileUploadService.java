package com.lookforbest.service;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final MinioClient minioClient;

    @Value("${app.storage.minio.bucket}")
    private String bucket;

    @Value("${app.storage.minio.endpoint}")
    private String endpoint;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/quicktime", "video/x-msvideo"
    );
    private static final Set<String> ALLOWED_MODEL_TYPES = Set.of(
            "model/gltf-binary", "model/gltf+json",
            "application/octet-stream" // .glb files often have this content type
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif",
            "mp4", "mov", "avi",
            "glb", "gltf"
    );

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB

    public String upload(MultipartFile file) throws Exception {
        validateFile(file);
        ensureBucketExists();

        String extension = getExtension(file.getOriginalFilename());
        String folder = resolveFolder(extension);
        String objectName = folder + "/" + UUID.randomUUID() + "." + extension;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        return endpoint + "/" + bucket + "/" + objectName;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 50MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("不支持的文件类型: " + ext + "。支持：jpg/png/webp/gif/mp4/mov/avi/glb/gltf");
        }
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            // Set public read policy
            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [{
                        "Effect": "Allow",
                        "Principal": {"AWS": ["*"]},
                        "Action": ["s3:GetObject"],
                        "Resource": ["arn:aws:s3:::%s/*"]
                      }]
                    }
                    """.formatted(bucket);
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucket).config(policy).build());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("无法识别文件扩展名");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String resolveFolder(String extension) {
        return switch (extension) {
            case "jpg", "jpeg", "png", "webp", "gif" -> "images";
            case "mp4", "mov", "avi" -> "videos";
            case "glb", "gltf" -> "models";
            default -> "files";
        };
    }
}
