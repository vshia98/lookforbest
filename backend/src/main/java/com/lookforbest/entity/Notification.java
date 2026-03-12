package com.lookforbest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {

    /** 通知类型枚举 */
    public enum NotificationType {
        inquiry_replied,   // 询价被回复
        comment_replied,   // 评论被回复
        system,            // 系统公告
        price_drop         // 降价提醒
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** 关联的业务对象 ID（如 inquiryId、commentId） */
    @Column(name = "related_id")
    private Long relatedId;

    /** 关联对象类型（inquiry / comment） */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
