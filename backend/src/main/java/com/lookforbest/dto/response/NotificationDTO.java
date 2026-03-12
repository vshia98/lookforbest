package com.lookforbest.dto.response;

import com.lookforbest.entity.Notification;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDTO {

    private Long id;
    private String type;
    private String title;
    private String content;
    private Boolean isRead;
    private Long relatedId;
    private String relatedType;
    private LocalDateTime createdAt;

    public static NotificationDTO from(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .content(n.getContent())
                .isRead(n.getIsRead())
                .relatedId(n.getRelatedId())
                .relatedType(n.getRelatedType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
