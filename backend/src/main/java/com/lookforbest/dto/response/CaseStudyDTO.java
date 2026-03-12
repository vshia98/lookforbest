package com.lookforbest.dto.response;

import com.lookforbest.entity.CaseStudy;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CaseStudyDTO {

    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private String title;
    private String content;
    private List<Long> robotIds;
    private String industry;
    private List<String> images;
    private String status;
    private String rejectReason;
    private Long viewCount;
    private Integer likeCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CaseStudyDTO from(CaseStudy caseStudy, boolean liked) {
        String authorName = caseStudy.getUser().getDisplayName() != null
                ? caseStudy.getUser().getDisplayName()
                : caseStudy.getUser().getUsername();
        return CaseStudyDTO.builder()
                .id(caseStudy.getId())
                .userId(caseStudy.getUser().getId())
                .authorName(authorName)
                .authorAvatar(caseStudy.getUser().getAvatarUrl())
                .title(caseStudy.getTitle())
                .content(caseStudy.getContent())
                .robotIds(caseStudy.getRobotIds())
                .industry(caseStudy.getIndustry())
                .images(caseStudy.getImages())
                .status(caseStudy.getStatus().name())
                .rejectReason(caseStudy.getRejectReason())
                .viewCount(caseStudy.getViewCount())
                .likeCount(caseStudy.getLikeCount())
                .liked(liked)
                .createdAt(caseStudy.getCreatedAt())
                .updatedAt(caseStudy.getUpdatedAt())
                .build();
    }
}
