package com.lookforbest.service;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileUploadService 单元测试")
class FileUploadServiceTest {

    @Mock
    private MinioClient minioClient;

    private FileUploadService fileUploadService;

    @BeforeEach
    void setUp() {
        fileUploadService = new FileUploadService(minioClient);
        ReflectionTestUtils.setField(fileUploadService, "bucket", "lookforbest");
        ReflectionTestUtils.setField(fileUploadService, "endpoint", "http://localhost:9000");
    }

    @Test
    @DisplayName("upload - 合法 JPEG 图片上传成功，返回完整 URL")
    void upload_validJpeg_returnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", new byte[1024]);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fileUploadService.upload(file);

        assertThat(url).startsWith("http://localhost:9000/lookforbest/images/");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    @DisplayName("upload - 合法 PNG 图片上传成功")
    void upload_validPng_returnsUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.png", "image/png", new byte[2048]);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fileUploadService.upload(file);

        assertThat(url).contains("/images/");
        assertThat(url).endsWith(".png");
    }

    @Test
    @DisplayName("upload - MP4 视频文件上传到 videos 目录")
    void upload_validMp4_savesToVideosFolder() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.mp4", "video/mp4", new byte[1024 * 100]);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fileUploadService.upload(file);

        assertThat(url).contains("/videos/");
        assertThat(url).endsWith(".mp4");
    }

    @Test
    @DisplayName("upload - GLB 3D 模型文件上传到 models 目录")
    void upload_validGlb_savesToModelsFolder() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "robot.glb", "model/gltf-binary", new byte[1024 * 50]);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fileUploadService.upload(file);

        assertThat(url).contains("/models/");
        assertThat(url).endsWith(".glb");
    }

    @Test
    @DisplayName("upload - 空文件抛出 IllegalArgumentException")
    void upload_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> fileUploadService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("文件不能为空");
    }

    @Test
    @DisplayName("upload - 文件超过 50MB 抛出 IllegalArgumentException")
    void upload_fileTooLarge_throwsException() {
        byte[] largeContent = new byte[51 * 1024 * 1024]; // 51MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.jpg", "image/jpeg", largeContent);

        assertThatThrownBy(() -> fileUploadService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("50MB");
    }

    @Test
    @DisplayName("upload - 不支持的文件类型抛出异常")
    void upload_unsupportedType_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.pdf", "application/pdf", new byte[1024]);

        assertThatThrownBy(() -> fileUploadService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的文件类型");
    }

    @Test
    @DisplayName("upload - 文件无扩展名抛出异常")
    void upload_noExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "noextension", "image/jpeg", new byte[1024]);

        assertThatThrownBy(() -> fileUploadService.upload(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("扩展名");
    }

    @Test
    @DisplayName("upload - Bucket 不存在时自动创建")
    void upload_bucketNotExists_createsAndUploads() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.webp", "image/webp", new byte[512]);

        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = fileUploadService.upload(file);

        assertThat(url).contains("/images/");
        verify(minioClient).makeBucket(any());
        verify(minioClient).setBucketPolicy(any());
    }
}
