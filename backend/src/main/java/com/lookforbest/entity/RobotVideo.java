package com.lookforbest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "robot_videos")
@Getter
@Setter
public class RobotVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_id", nullable = false)
    private Robot robot;

    @Column(length = 300)
    private String title;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 500)
    private String thumbnail;

    @Column(name = "duration_s")
    private Integer durationS;

    @Enumerated(EnumType.STRING)
    @Column(name = "video_type", length = 20)
    private VideoType videoType = VideoType.product;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum VideoType { product, demo, installation, official }
}
