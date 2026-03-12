package com.lookforbest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "robot_3d_models")
@Getter
@Setter
public class RobotModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "robot_id", nullable = false, unique = true)
    private Long robotId;

    /** GLB/GLTF 模型文件 URL */
    @Column(name = "model_url", length = 500)
    private String modelUrl;

    /** 预览图 URL */
    @Column(name = "poster_url", length = 500)
    private String posterUrl;

    /** USDZ for iOS AR */
    @Column(name = "ar_url", length = 500)
    private String arUrl;

    @Column(length = 200)
    private String title;

    /** 标注点 JSON */
    @Column(columnDefinition = "JSON")
    private String annotations;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
