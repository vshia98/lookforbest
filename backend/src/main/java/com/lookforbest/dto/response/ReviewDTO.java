package com.lookforbest.dto.response;

import com.lookforbest.entity.RobotReview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReviewDTO {

    private Long id;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long robotId;
    private String robotName;
    private String robotSlug;
    private String title;
    private String content;
    private String pros;
    private String cons;
    private int rating;
    private List<String> images;
    private String status;
    private String rejectReason;
    private Long viewCount;
    private Integer likeCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewDTO from(RobotReview review, boolean liked) {
        String authorName = review.getUser().getDisplayName() != null
                ? review.getUser().getDisplayName()
                : review.getUser().getUsername();
        return ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUser().getId())
                .authorName(authorName)
                .authorAvatar(review.getUser().getAvatarUrl())
                .robotId(review.getRobot().getId())
                .robotName(review.getRobot().getName())
                .robotSlug(review.getRobot().getSlug())
                .title(review.getTitle())
                .content(review.getContent())
                .pros(review.getPros())
                .cons(review.getCons())
                .rating(review.getRating() != null ? review.getRating() : 5)
                .images(review.getImages())
                .status(review.getStatus().name())
                .rejectReason(review.getRejectReason())
                .viewCount(review.getViewCount())
                .likeCount(review.getLikeCount())
                .liked(liked)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
